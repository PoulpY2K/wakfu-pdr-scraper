# JSON state store and scrape pipeline: design

Date: 2026-10-17 · Status: validated by the owner during brainstorming (not yet formally reviewed as a file) · Class names below are indicative; the code is the reference (`PlaywrightManager`, `CategoryParser`, `Topic`).

## Goal

Run unattended on a VPS, detect new roleplay activity on the Wakfu forum and announce it on Discord webhooks. The only state needed is "what was seen last time". PostgreSQL is dropped in favour of a JSON file.

## Decisions

- **Storage:** a single JSON file on a Docker volume. Rejected: PostgreSQL (a second container to run for this app alone, history not needed) and an embedded SQL file (extra dependencies for lookups that are plain map reads).
- **What is announced:** for a **new topic**, the content of the topic's own first post (the original post, never the replies); for a **new reply** in any topic, the latest reply only.
- **What triggers a message:**
  - a **new topic** in the category `583-histoire-jeu-role`: a non-pinned row with an id above `lastTopicId`;
  - a **new reply** in any non-pinned topic currently shown on page 1 of the category, or in one of the two special pinned threads (Rumeurs, Tableau d'affichage), detected by its "Réponses" count going up. Because the list is sorted by activity, every topic that just received a reply is on page 1, so no separate history of "recent topics" is needed. **The other pinned topics are ignored.**
- **Webhooks:** new topics and replies in ordinary topics go to `global`; replies in `395506-rp-rumeurs` go to `rumors`; replies in `348768-annonces-rp-tableau-affichage` go to `board`.
- **Cheap change detection:** reply counts come from the category table that is fetched anyway. A topic page is opened **only when its count went up** (via `?last`, to read the latest reply) or when the topic is new (to read its original post). Nothing else is fetched.
- Notifications are posted before the state is saved (at-least-once: a crash may repeat one message, never lose one).
- Post ID order is irrelevant: the latest reply is simply the last reply displayed on the last page (the board fixture has non-monotonic IDs, which is why IDs are never compared).

## Forum structure (verified on the fixtures in `src/test/resources/fixtures`)

Category page (`583-histoire-jeu-role`):
- Topics are `tr` rows, 50 per page, paginated (`?page=N`), ordered by activity (last post), pinned topics first (`tr.ak-pinned-topic`).
- Columns: topic (title link `a.ak-title-topic`, author `a.ak-author-nickname`, creation date in `.ak-desc-topic`), **Réponses**, Votes, Vues, Activité (link ending `?last`).
- Title link href: `https://www.wakfu.com/fr/forum/<subforum>/<topicId>-<slug>`; the subforum varies (`584-general`, `586-ogrest`). The two special threads are recognised by their topic ID (`395506`, `348768`), taken from the configured paths, only to choose their webhook. They are pinned, so they stay on page 1.
- Fixture values: Rumeurs 589 replies, Tableau d'affichage 816 replies. Their "Activité" times (25/09/2026 21:56:17 and 06/09/2026 20:34:06) equal the date of the last post shown on the saved last pages, which confirms that the last displayed post is the latest one.
- Reply counts are not the number of posts on the thread pages (Rumeurs: 589 vs 25 × 25 + 5 = 630 posts). Only compare a count with itself over time, always read from this table.
- New topics: ordering is by activity, so an old topic that gets a reply moves to page 1 and the **first non-pinned row is not the newest topic** (in the fixture it is `422556`, while the newest is `435756`). A set of "seen IDs" is not usable either. Topic IDs look like one forum-wide increasing sequence, so the state keeps a watermark: the highest topic ID seen among non-pinned rows. To verify against more data before relying on it.

Topic / thread page:
- `<thread-url>?last` opens the **last page of the thread** (confirmed by the owner).
- **Original post (page 1 only):** it is *not* a `div.ak-post`. It is `div.ak-first-post` (`id="entry<postId>"`), body in `div.ak-text` inside `.ak-item-mid`. Its author and date are **not** in the block but in the page header: `h1` title, then `.ak-subtitle` containing `a.ak-author-nickname` (nickname plus `span.ak-nickname-tag`, e.g. `Unkar#3113`) and a French-formatted date text such as `02 Août 2020 - 19:49:57` (same format as the category table; parse with a French locale).
- **Replies:** `div.ak-post` with `id="entry<postId>"`; author `a.ak-author-nickname`; date `span.ak-date` (title attribute `dd/MM/yyyy - HH:mm:ss`, text in French format); body `div.ak-text`; permalink `<thread-url>?poid=<postId>`. The latest reply is the last `div.ak-post` of the last page. A topic is only opened for replies when its reply count went up, so at least one `div.ak-post` exists.
- A page can hold both kinds (a single-page topic has the original post plus its replies), so the parser must keep them apart: announcing a new topic uses `.ak-first-post` only, announcing a reply uses the last `.ak-post` only.

## Storage

File `wakfu.state.file` (default `/data/state.json`; dev: `./config-local/state.json`, git-ignored and docker-ignored):

```json
{
  "version": 1,
  "lastTopicId": 435756,
  "replyCounts": { "395506": 589, "348768": 816, "435756": 0, "422556": 5 }
}
```

`replyCounts` maps a topic id to its last seen "Réponses" count, for every non-pinned row of page 1 plus the two special pinned threads. It is rebuilt from the page at each tick, so it never holds more than about 50 entries.

Units:
- `SnapshotStore` (interface): read and save `lastTopicId` and the reply counts.
- `JsonFileSnapshotStore`: loads once at startup, keeps state in memory, serialises writes (one lock), writes `state.json.tmp` then atomic-moves over `state.json`.
- A missing file means empty state. A corrupt file is renamed `state.json.corrupt-<timestamp>`, a warning is logged, and the app starts empty.
- **First run:** with no state at all, record `lastTopicId` (newest non-pinned id) and the counts of every tracked row, announce nothing, so a fresh start never floods Discord.

## Pipeline (one tick)

All watched items live in the same category page, so a single scheduled tick fetches it **once** (the current scheduler method already does).

1. **Fetch** (`ForumFetcher`, built on `PlaywrightManager`): loads a URL with a configured user agent and the stealth scripts, fresh browser context per run, timeouts, returns HTML. Blocked or failed loads raise a typed `ScrapeException`.
2. **Parse** (jsoup, pure functions, tested on fixtures):
   - `CategoryParser`: HTML to topic rows (id, title, url, author, created, replies, pinned flag).
   - `ThreadParser`: a topic page to its original post (`.ak-first-post`, with author and date from the header, used for new topics) and to its replies (`.ak-post`, the last one is the one announced); each with id, author, date, text, permalink.
3. **Diff** (pure functions), for each tracked row of page 1 (every non-pinned row, plus Rumeurs and Tableau d'affichage; other pinned rows are skipped):
   - **New topic**: non-pinned and `id > lastTopicId`. Processed oldest first; the topic is opened and its **original post** is read for the message. The watermark moves to its id after Discord accepted the message.
   - **Known topic** (id present in the stored `replyCounts`): count higher than stored → open `<topic-url>?last` and announce the latest reply; count lower (post deleted) → store silently; equal → nothing.
   - **Unknown, not new** (an old topic coming back from page 2): store its count silently, announce nothing, because its previous count is unknown. A single reply can be missed this way, which is acceptable at this activity level.
   - If several replies arrive in one topic between two ticks, only the latest is announced.
4. **Notify** (`DiscordNotifier`, `RestClient` with the generated Discord DTOs): new topics and replies in ordinary topics go to the `global` webhook, replies in Rumeurs / Tableau d'affichage to the `rumors` / `board` webhook. Embeds say whether it is a new topic or a reply and name the topic, use the configured bot name and avatar, are truncated to Discord limits, and honour HTTP 429 `Retry-After`.
5. **Commit:** the new `replyCounts` are rebuilt from page 1 (topics that left page 1 disappear). A row whose announcement or page fetch failed keeps its **old** count (a failed new topic is simply left out and the watermark stops before it), so the next tick retries it. Each value is saved only after Discord accepted its message.

Scheduling: one scheduled method, enabled by `scheduler.enabled` and run every `scheduler.rate` minutes (as implemented in `WakfuTopicScheduler`); the three per-section interval properties no longer exist.

## Error handling

- The scheduled method catches and logs everything; one failed tick never stops later ticks.
- Sanity guard: a category page that parses to zero topic rows, when state exists, is treated as a layout change or block page; log an error and leave state untouched.

## Configuration, deployment, removals

- New: `wakfu.state.file`. Already in place: `scheduler.enabled`, `scheduler.rate` (minutes).
- Removed: `datasources.pdr.*`, `config-local/libs/datasources.yaml`, `spring-boot-starter-data-jpa`, `postgresql`, `h2`, the `persistence` configuration package, `DatasourceConstants`, `DataSourceAutoConfiguration` exclusion, `spring.jpa.*`, the `db` health indicator and healthcheck group, `docker-compose-dev.yaml` and its run configuration, the H2 block of `application-test.yaml`, the three `wakfu.scheduler.{rp,rumors,board}.fixed-delay` properties. (All of this is already removed.)
- Dockerfile: create `/data` owned by `wakfu`, declare it a `VOLUME`. The VPS deploy script mounts a named volume on it. `state.json*` is added to `.gitignore` and `.dockerignore`.

## Testing

- JSON store: round trip, atomic write, missing file, corrupt file, first-run behaviour.
- `CategoryParser` on the category fixture (50 topic rows, pinned flags, topic IDs across subforums, replies 589 and 816 for the two threads). `ThreadParser` on the `rumors` last page (26 of 26, latest reply `3599535`), the `board` last page (35 of 35, latest displayed reply `3599144`, non-monotonic IDs) and the `[GLOBAL] Panneaux Mortuaires` topic page (topic `422556`, single page: original post `3509780` by Unkar#3113, dated 02 Août 2020 - 19:49:57, which is the one announced; 5 replies `3516355`, `3520255`, `3524431`, `3599742`, `3599827` that are ignored).
- Diff: pure unit tests (first run, no change, new topic, bumped old topic, reply in an ordinary topic, reply in Rumeurs / Tableau d'affichage routed to their webhooks, count down, count equal, old topic returning from page 2 recorded silently, topic leaving page 1 dropped from the state, failure keeps the old count, zero-row guard).
- Notifier: mock HTTP server for payloads, truncation and 429 retry.
- `contextLoads` stays green after the removals.

## Out of scope

History or statistics, several app instances, a web UI, replies in topics that are not on page 1 of the category, announcing every reply when several arrive between two ticks (only the latest is announced).
