# AGENTS.md

Guidance for AI coding agents working on **wakfu-pdr-scraper** (`fr.fumbus:wakfu-pdr-scraper`, MIT).

## Purpose

A Spring Boot service that watches the Wakfu **roleplay** forum (`https://www.wakfu.com/fr/forum`) and announces **new posts/topics** on Discord through webhooks ("Potion de Rappel" bot identity).

Intended flow (target design, see `docs/superpowers/specs/2026-10-17-json-state-store-design.md`):

1. One scheduler tick fetches the roleplay category page once (Playwright to load the page, jsoup to parse).
2. It is compared with the **previous state stored in a JSON file** (PostgreSQL was dropped, see "Storage decision" below).
3. Anything new is posted to the matching Discord webhook, then the state is saved.
4. It must run **passively and unattended on a VPS** (Docker container, auto-deployed by CI). Favour robustness over cleverness: never let one failed scrape/webhook kill the scheduler, log clearly, avoid duplicate or flooding notifications (e.g. on first run record the current state without announcing anything, persist state so a restart does not re-announce).

What is watched (paths relative to `wakfu.forum.url`; everything comes from the one category page, so one scheduler tick is enough):

| Item | Config key | Path | What triggers a message |
|---|---|---|---|
| Category (many topics, 50 per page, sorted by last activity) | `wakfu.forum.rp.path` | `/583-histoire-jeu-role` | a **new topic** (non-pinned id above `lastTopicId`), or a **new reply** in any non-pinned topic shown on page 1 (its "Réponses" count went up) |
| Rumeurs thread (pinned) | `wakfu.forum.rumors.path` | `/584-general/395506-rp-rumeurs` | its reply count went up |
| Tableau d'affichage thread (pinned) | `wakfu.forum.board.path` | `/584-general/348768-annonces-rp-tableau-affichage` | its reply count went up |

Webhooks (`discord.webhook.*`): new topics and replies in ordinary topics go to `global`; replies in Rumeurs go to `rumors`, in Tableau d'affichage to `board`. The other pinned topics and any topic that is not on page 1 are out of scope.

## Forum scraping rules (verified on the fixtures)

- **The forum sits behind an AWS WAF bot challenge.** The first response to a page request is a small challenge page (`#challenge-container`, `AwsWafIntegration`, no forum content) whose script gets a token and reloads. A plain HTTP client or an early `page.content()` only sees that page: `PlaywrightManager` waits for `#challenge-container` to disappear before returning the HTML. If a fetch ever returns a page without topic rows, suspect the challenge (or a block) first. Every fetch costs a browser launch plus the challenge (about 4 s), so only open topic pages when needed and never hit the real forum from tests.
- **Detect new replies from the category table, not from the topic pages.** Keep the last seen **"Réponses"** count per topic id for every non-pinned row of page 1, plus the two special pinned threads (Rumeurs `395506`, Tableau d'affichage `348768`). **All other pinned topics are ignored.** Only if a count went up, fetch `<topic-url>?last` (confirmed to land on the topic's last page) and announce **the latest reply only** (the last `div.ak-post` of the page). Equal count: do nothing. Lower count (deleted post): store it silently. A topic not yet in the stored map that is not new (an old topic coming back from page 2): store its count silently, announce nothing. Rebuild the map from page 1 at each tick (it stays under about 50 entries). The table count is not the number of posts on the topic pages (589 vs 630 for Rumeurs), so only ever compare it with itself over time.
- **Post IDs are not monotonic in display order** (the board fixture lists `3596158` before `3595514`): never compare post IDs, use the last displayed reply.
- **New topics: use a "highest topic ID seen" watermark, not a set of seen IDs, and never "the first non-pinned row".** The listing is ordered by activity, so an old topic that gets a reply jumps to the top (in the fixture the first non-pinned row is `422556`, the newest topic is `435756`). Ignore pinned rows (`tr.ak-pinned-topic`) for new-topic detection. For each new topic (oldest first), open its URL and announce the content of its **first post, i.e. the original post, not the replies**; advance the watermark only after Discord accepted the message. (Replies are the opposite: announce the latest reply.)
- Useful selectors: topic title link `a.ak-title-topic` (href `.../<subforum>/<topicId>-<slug>`, the subforum varies); table columns Réponses / Votes / Vues / Activité after the title. On a topic page, the **original post** is `div.ak-first-post` (id `entry<postId>`, body `div.ak-text`) and its author/date live in the header (`.ak-subtitle`: `a.ak-author-nickname` + French date text like `02 Août 2020 - 19:49:57`), not in the post block. **Replies** are `div.ak-post` (`id="entry<postId>"`, author `a.ak-author-nickname`, date `span.ak-date` with title `dd/MM/yyyy - HH:mm:ss`, body `div.ak-text`, permalink `<thread>?poid=<postId>`). Never mix the two: new topic = `.ak-first-post`, reply = last `.ak-post`.
- Real saved pages live in `src/test/resources/fixtures/`: category page 1, `rumors` last page (26/26), `board` last page (35/35), and a regular topic page (`[GLOBAL] Panneaux Mortuaires`, topic `422556`: original post `3509780` by Unkar#3113, 5 replies; used to test reading a new topic's original post). Parsers must be tested against real pages; do not invent HTML. If you need another page type, ask the owner to save it there.
- Category page with zero topic rows when state exists = layout change or block page: log an error and do not touch the state.

## Storage decision

State is a single JSON file (`wakfu.state.file`, default `/data/state.json` on a Docker volume), not PostgreSQL: only the "last seen" state is needed (`lastTopicId` watermark, plus a `replyCounts` map `topicId → count` for the tracked rows of page 1), no history. Write atomically (temp file + move), serialise writes, treat a missing file as empty and quarantine a corrupt one. Save each value only after Discord accepted its message (at-least-once).

**The JSON state store is not implemented yet** (see the design spec). PostgreSQL, JPA, H2 and the datasource config have already been removed: do not bring them back.

## Current state (important)

Work in progress. What exists and what does not:

- **Done:** `PlaywrightManager.fetchHtml(url)` (one headless Chromium launch per call with `--disable-dev-shm-usage`, random user agent from config, the three `stealth/*.js` init scripts, waits out the AWS WAF challenge page before returning the HTML); `CategoryParser.parse(html)` returning `List<Topic>` (`Topic(id, title, url, replyCount, pinned)`), tested on the category fixture.
- `WakfuTopicScheduler.scrapeRoleplayForum` (one `@Scheduled` method, `scheduler.enabled` / `scheduler.rate` in minutes) fetches and parses the category page and finds the Rumeurs and Tableau d'affichage rows and the newest non-pinned topic id, but does nothing with them yet.
- **Not done:** reply-count comparison and the tick algorithm, topic page parsing (original post / latest reply), the JSON state store, Discord sending (DTOs are generated, see below; `spring-boot-starter-restclient` is available for `POST /webhooks/{id}/{token}`), the Docker `/data` volume, ignoring `state.json` in `.gitignore` / `.dockerignore`.
- Next steps and their order: `docs/superpowers/plans/2026-10-17-forum-watcher.md` (global directives and algorithms; class names and structure are the owner's call).
- Tests: `WakfuPdrScraperApplicationTests.contextLoads`, `CategoryParserTest`. Fixtures for the other parsers are in place (see above). No test may reach the real forum; the test profile keeps `scheduler.enabled: false`.

## Tech stack

- Java **25**, Maven, Spring Boot **4.1.x** (Jackson **3**: imports are `tools.jackson.*`, not `com.fasterxml.jackson.databind`).
- No database: state will live in a JSON file.
- Playwright (Java) for browsing, jsoup for HTML parsing, Lombok, MapStruct (`componentModel=spring`), commons-lang3.
- **Log4j2** (Spring's default logging starter is excluded from every starter — keep excluding it when adding starters).
- Actuator on a separate management port **8081** (`health,info,metrics,caches`); virtual threads enabled.
- Discord DTOs are **generated** by `openapi-generator-maven-plugin` from `src/main/resources/generated/client-api-spec/discord/Discord-v10.json` into `target/generated-sources` (package `fr.fumbus.wakfupdrscraper.dtos.discord`, `*Dto` suffix, Lombok `@Builder`/`@Jacksonized`, models only, no APIs). Never edit generated code or commit `target/`.

## Layout

```
src/main/java/fr/fumbus/wakfupdrscraper/
  WakfuPdrScraperApplication.java        # @EnableScheduling @EnableAsync
  configurations/ObjectMapperConfiguration.java   # primary Jackson 3 JsonMapper: snake_case, NON_NULL, trims strings, ignores unknown props
  models/Topic.java                       # a row of the category table
  models/ThreadType.java                  # RUMORS, BOARD
  models/properties/                      # Discord/Wakfu (@Value-based), Playwright (@ConfigurationProperties "playwright")
  parsers/CategoryParser.java             # category HTML -> List<Topic>
  playwright/PlaywrightManager.java       # fetchHtml(url)
  schedulers/WakfuTopicScheduler.java
src/main/resources/                       # application.yaml, log4j2-spring.yml, banner, stealth/*.js, Discord OpenAPI spec
src/test/resources/application-test.yaml  # full test config (dummy webhooks, scheduler disabled)
src/test/resources/fixtures/              # real saved forum pages used by parser tests
config-local/                             # dev-only external config (see Configuration)
```

There is no DataSource and no database dependency.

## Configuration

`src/main/resources/application.yaml` holds only generic settings. Everything environment-specific lives outside the jar and is **required** (the app fails to start without it):

- `discord.name`, `discord.profile-picture-url`, `discord.webhook.{global,board,rumors}.{id,token}`
- `wakfu.forum.url`, `wakfu.forum.{rp,rumors,board}.path`
- `playwright.headless`, `playwright.user-agents`
- `fumbus.logger.level.main` (read by `log4j2-spring.yml`)
- `scheduler.enabled` (boolean) and `scheduler.rate` (**minutes**, default 60), read by `WakfuTopicScheduler` (`@ConditionalOnBooleanProperty` + `@Scheduled(fixedRateString=…)`). The state file path (`wakfu.state.file`) will be added with the JSON store.

Dev setup (profile `dev`): `config-local/application-dev.yaml` imports `config-local/libs/*.yaml`; webhook ids/tokens come from `config-local/libs/.env` (**git-ignored and docker-ignored, contains real secrets — never commit, print or copy its values**). Run from the repo root with  
`-Dspring.config.additional-location=./config-local/ -Dspring.profiles.active=dev` (see `.run/WakfuPdrScraper-dev.run.xml`).

Production configuration is not in the repo (the Dockerfile only copies the jar). It must be supplied on the VPS through environment variables / mounted config / the `SSH_SCRIPT` deploy secret. Never hard-code credentials or webhook tokens in tracked files.

## Build, test, run

```
mvn clean verify --no-transfer-progress -B     # what CI runs (build + tests + JaCoCo)
mvn test                                       # unit/context tests (profile `test`, no external service needed)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.config.additional-location=./config-local/ -Dspring.profiles.active=dev"
```

- No Maven wrapper is committed; use a system `mvn` with JDK 25.
- The POM version is `MANAGE_BY_EXTENSION`: `maven-git-versioning-extension` derives it from git (tag → `<tag>`, branch → `<branch>-SNAPSHOT`). `.git-versioned-pom.xml` is generated and ignored.
- Tests use `@SpringBootTest` + `@ActiveProfiles("test")`. New config keys that beans require must also be added to `src/test/resources/application-test.yaml`, otherwise `contextLoads` breaks. The surefire config injects the Mockito agent copied by `maven-dependency-plugin`; keep that when editing the POM.

## CI/CD and deployment

- `.github/workflows/maven-build-analyze.yml`: `mvn clean verify` on push/PR, SonarQube analysis on `main` and PRs, uploads the jar artifact, submits dependency graph.
- `.github/workflows/docker-publish-deploy.yml`: after a successful push build on a non-`main` ref/tag, builds the image from the jar (`Dockerfile`, Temurin 25 JRE + Playwright Chromium, non-root user `wakfu`, `JAVA_OPTS=-Xmx2G -XX:+UseZGC`), pushes to `ghcr.io`, signs it with cosign, and — only when a `latest` tag was produced (semver release tags) — SSHes into the VPS and runs `secrets.SSH_SCRIPT`.
- Container health check: `GET :8081/actuator/health/liveness`.
- Dependabot keeps Maven deps and Actions up to date. Several versions are pinned in the POM purely to override vulnerable transitive deps (`tomcat`, `jackson-bom`, `asm`, …); don't downgrade them.
- **Playwright in Docker**: the `Dockerfile` installs Chromium and its system libraries at build time (`install --with-deps chromium` through Spring's `PropertiesLauncher` + `loader.main=com.microsoft.playwright.CLI`) into `PLAYWRIGHT_BROWSERS_PATH=/ms-playwright`, readable by the non-root `wakfu` user. Keep this step when editing the Dockerfile; the browser version automatically follows the `playwright.version` bundled in the jar, so nothing to bump separately. Chromium may crash on the default 64 MB `/dev/shm`: `PlaywrightManager` already launches with `--disable-dev-shm-usage`; a larger `shm_size` on the container is a further option.

## Conventions

- Every class carries the author Javadoc header (`@author Jérémy Laurent <poulpy2k>` / `@see "https://github.com/poulpy2k"`).
- Lombok everywhere: `@RequiredArgsConstructor` constructor injection, `@Log4j2` for logging (not SLF4J loggers by hand), `@UtilityClass` for constants. `lombok.config` sets `addLombokGeneratedAnnotation` and Jackson 3 for `@Jacksonized`.
- Config classes: external-config POJOs live in `models.properties`; HTML parsers are stateless `@UtilityClass`es in `parsers`, tested against the saved pages in `src/test/resources/fixtures`.
- Code comments only where logic is non-obvious. Logs and the forum are French; code, identifiers and docs are English.
- Be a polite scraper: keep reasonable intervals, rotate the configured user agents, handle timeouts and HTML structure changes gracefully (log and skip rather than crash), and truncate/escape forum text for Discord limits (2000-char content, embed limits) and respect webhook rate limits (HTTP 429 `Retry-After`).

## Working with the owner

- Keep tasks small and strictly in the scope that was asked; do not add extra features, abstractions or refactors unasked.
- The owner often implements things himself: when asked for a plan, give ordered global directives and the algorithm, **not** class names, file layouts or full code, unless asked.
- Do not commit unless asked (commits are GPG-signed and need an interactive pinentry). Never read, print or copy `config-local/libs/.env`.
- Before claiming something works, run it (`mvn -B test`); the first Playwright run downloads Chromium (about 150 MB).

## Security notes

- Secrets (`config-local/libs/.env`, webhook tokens) must never be committed, logged, or echoed in answers.
- Actuator exposes `health,info,metrics,caches` with `show-details: always` on port 8081; do not publish that port on the VPS to the internet.
