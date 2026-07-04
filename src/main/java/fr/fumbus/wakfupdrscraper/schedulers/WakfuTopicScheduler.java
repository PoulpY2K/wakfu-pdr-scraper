package fr.fumbus.wakfupdrscraper.schedulers;

import fr.fumbus.wakfupdrscraper.playwright.PlaywrightManager;
import fr.fumbus.wakfupdrscraper.properties.WakfuProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class WakfuTopicScheduler {

    private final PlaywrightManager playwrightManager;
    private final WakfuProperties wakfuProperties;

    /**
     * Scrapes the Roleplay (RP) section of the Wakfu forum.
     */
    @Scheduled(fixedDelayString = "${wakfu.scheduler.rp.fixed-delay:60000}")
    public void scrapeRoleplay() {
        log.info("Starting Roleplay scrape…");
        String uri = wakfuProperties.getForumUrl() + wakfuProperties.getForumRoleplayPath();
        log.info("Target URI: {}", uri);
        // TODO: parse content with jsoup and persist topics
    }

    /**
     * Scrapes the Rumors section of the Wakfu forum.
     */
    @Scheduled(fixedDelayString = "${wakfu.scheduler.rumors.fixed-delay:60000}")
    public void scrapeRumors() {
        log.info("Starting Rumors scrape…");
        String uri = wakfuProperties.getForumUrl() + wakfuProperties.getForumRoleplayRumorsPath();
        log.info("Target URI: {}", uri);
        // TODO: parse content with jsoup and persist topics
    }

    /**
     * Scrapes the Board section of the Wakfu forum.
     */
    @Scheduled(fixedDelayString = "${wakfu.scheduler.board.fixed-delay:60000}")
    public void scrapeBoard() {
        log.info("Starting Board scrape…");
        String uri = wakfuProperties.getForumUrl() + wakfuProperties.getForumRoleplayBoardPath();
        log.info("Target URI: {}", uri);
        // TODO: parse content with jsoup and persist topics
    }
}
