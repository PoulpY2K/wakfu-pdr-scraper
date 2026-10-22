# Forum Watcher: plan

Reference: `docs/superpowers/specs/2026-10-17-json-state-store-design.md`. Structure, naming and tests are yours to decide. Real forum pages for testing are in `src/test/resources/fixtures/`.

## Status (update this as you go)

- **Done:** step 1 (scaffold removed), the category parser part of step 3 (`CategoryParser` + `Topic` + test), step 9 (`PlaywrightManager.fetchHtml`, see the note in that section), and the scheduler fetching and parsing the category page.
- **Next:** the core algorithm (step 7) with the state store (step 6), then topic page parsing (step 4), then Discord (steps 5 and 8), then Docker/hygiene and docs (steps 11, 12).
- The spec's class names (`ForumFetcher`, `ThreadParser`, `SnapshotStore`, `DiscordNotifier`, …) are only indicative: the code uses `PlaywrightManager`, `CategoryParser`, `Topic`. Follow the code.

## Order of work

1. **Remove the PostgreSQL/JPA scaffold.** Dependencies (JPA, Postgres, H2), persistence config, datasource constants and properties, the DB health indicator and its health group, the dev compose file and its run config. The app context must still load.
2. **Model the forum.** A topic row (id, title, url, reply count, pinned), a post (id, author, date, content, url), a category page with: look up a topic by id, regular (non-pinned) topics newer than an id (oldest first), newest regular topic id.
3. **Parse the category page** (details below). Zero topic rows is an error, never an empty result.
4. **Parse a topic page**: its original post and its latest reply (details below).
5. **Convert post HTML to Discord markdown** (rules below).
6. **Persist the state in a JSON file** (algorithm below).
7. **Write the core algorithm** (below), against abstractions for: reading the forum, announcing, and storing state. Test it with fakes.
8. **Send Discord messages** (below).
9. **Fetch pages with Playwright** (below), behind an abstraction so everything else is testable without a browser.
10. **Wire everything**: one scheduler tick calling the core algorithm, a state-file property, the two special topic ids (Rumeurs, Tableau d'affichage) derived from the configured paths for webhook routing. Tests must never reach the real forum (the test profile keeps the scheduler disabled).
11. **Docker and hygiene**: writable `/data` volume for the `wakfu` user, ignore the state file in git and docker.
12. **Docs and first run**: update `AGENTS.md` and the spec; first local run only records the baseline.

## Core algorithm (one tick)

1. Read the category page once.
2. **First run** (no stored state at all): store the newest non-pinned topic id as `lastTopicId` and the reply count of **every tracked row** (non-pinned rows plus Rumeurs and Tableau d'affichage), announce nothing, stop.
3. **New topics** (independent step): for each non-pinned topic with id greater than `lastTopicId`, oldest first: open it, take its **original post**, announce it, then store that topic's id as `lastTopicId`. On the first failure stop (the next tick retries from there).
4. **Replies** (independent step), for every other tracked row of page 1 (non-pinned rows plus Rumeurs and Tableau d'affichage; **other pinned rows are ignored**), compared with its stored count (map topic id → count):
   - count higher → open `<topic-url>?last`, take the **last reply**, announce it, then store the new count;
   - count lower (post deleted) → store it silently;
   - equal → nothing, and never open the topic;
   - no stored count (an old topic coming back from page 2) → store the count silently, announce nothing.
5. Rebuild the stored map from page 1 at each tick (topics that left page 1 are dropped). A row whose announcement or fetch failed keeps its old count so the next tick retries.
6. Every step is caught and logged on its own: one failure must not block the others, and the scheduler must never die.
7. Save a value only **after** Discord accepted the message (at-least-once: a crash may repeat one message, never lose one).

## Category page parsing

- A topic is a table row containing a topic title link; take id from the link's `/<id>-<slug>` path segment, title from the link text, reply count from the **second column** (digits only), pinned if the row has the pinned class.
- Pinned rows include the two special threads (Rumeurs, Tableau d'affichage): they are never "new topics", but their reply counts are tracked like any other row. The other pinned rows (utility, definition topics) are ignored entirely.
- Topic order is by activity, not creation: that is why new topics use an id watermark, never a set of seen ids.

## Topic page parsing

- **Original post**: the dedicated first-post block. Its author and date are **not** inside it but in the page header, under the title (French date such as `02 Août 2020 - 19:49:57`). Content = the post body of that block.
- **Latest reply**: the last reply block of the page; author from the nickname link, date from the numeric date attribute (`dd/MM/yyyy - HH:mm:ss`), content from the post body. Permalink = page url without query + `?poid=<id>`.
- Never compare post ids with each other (their order is not monotonic); "latest" means last displayed.
- Forum dates are `Europe/Paris`. Nicknames come with their tag (`Name#1234`): keep it.
- Missing expected elements → clear error (the layout changed or a block page was served).

## Post content → Discord markdown

Bold → `**`, italic → `*`, underline → `__`, line break → newline, paragraph/div/list → blocks separated by a blank line, list item → `- `, absolute link → `[text](url)` (otherwise just the text), images and scripts dropped, non-breaking space → space, no leading spaces on lines, at most one blank line in a row, whitespace kept outside the markers (`**bold** text`, not `**bold **text`), empty emphasis dropped.

## State file

- Content: the last topic id and a map topic id → last seen reply count (tracked rows of page 1). The app's JSON mapper is snake_case.
- Load once at startup. Missing file = empty state. Unreadable content → rename it `<file>.corrupt-<timestamp>`, log a warning, start empty.
- Every write: create parent directories, write a temporary file next to it, then atomically move it over the real one. Serialise writes.

## Discord messages

- One embed per announcement. Title: `Nouveau sujet : <topic title>` or `Nouvelle réponse : <topic title>` (max 256). Description = post content (max 4096, ends with `…` when cut, omitted when blank). Link = post url, author = post author, timestamp = post date. Bot name and avatar from config.
- New topics and replies in ordinary topics go to the global webhook; replies in Rumeurs go to the rumors webhook, in Tableau d'affichage to the board webhook.
- Send with the generated Discord DTOs. On HTTP 429 wait `Retry-After` (1 s if absent) and retry, 3 attempts maximum. An I/O error message contains the webhook URL with its token: rethrow without it. Never log tokens.

## Playwright fetching (done: `PlaywrightManager.fetchHtml`)

- Implemented as one headless Chromium launch per call (acceptable at a 60-minute rate; `--disable-dev-shm-usage` for Docker), nothing starts at application startup. Reuse of the browser between calls is a possible later optimisation, not needed now.
- One fresh browser context per fetch: random user agent from config, the three stealth scripts as init scripts, navigate waiting for DOM content, fail clearly on a missing or non-OK response, return the page HTML, close everything.
- **AWS WAF challenge:** the forum first answers with a challenge page (`#challenge-container`, `AwsWafIntegration`) that computes a token and reloads. The fetch waits (30 s max) until `#challenge-container` is gone, then returns the real page. Verified once against the real category page (50 topic links).
- Parse the HTML with the requested URL as base URI (permalinks depend on it).

## Checks

- After each step the build stays green.
- Parser expectations from the fixtures: category page = 50 rows, 6 pinned, Rumeurs (395506) 589 replies, Tableau d'affichage (348768) 816 replies, newest regular topic 435756; Rumeurs last reply `3599535` by `Affabulatus#8053`; Tableau d'affichage last reply `3599144` by `XARAN#2294`; topic `422556` original post `3509780` by `Unkar#3113` (02/08/2020 19:49:57) and its last reply `3599827` by `Scyanomage`.
- Core algorithm cases: first run announces nothing; several new topics announced oldest first; failure keeps the stored id; reply in an ordinary topic (global webhook) and in Rumeurs / Tableau d'affichage (their webhooks); count up / equal / lower; old topic returning from page 2 recorded silently; topic leaving page 1 dropped from the state; a failing step doesn't stop the others.
- First real run: it only records the baseline. To see real messages, edit the state file (lower the topic id and a reply count) and use a **test** webhook, not the production ones.
