package fr.fumbus.wakfupdrscraper.schedulers;

import fr.fumbus.wakfupdrscraper.models.Topic;
import fr.fumbus.wakfupdrscraper.models.properties.WakfuProperties;
import fr.fumbus.wakfupdrscraper.parsers.CategoryParser;
import fr.fumbus.wakfupdrscraper.playwright.PlaywrightManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@Log4j2
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(name = "scheduler.enabled")
public class WakfuTopicScheduler {

    private static final long ID_TOPIC_RUMORS = 395506;
    private static final long ID_TOPIC_BOARDS = 348768;

    private final PlaywrightManager playwrightManager;
    private final WakfuProperties wakfuProperties;

    /**
     * Scrapes the Roleplay (RP) section of the Wakfu forum.
     */
    @Scheduled(fixedRateString = "${scheduler.rate:60}", timeUnit = TimeUnit.MINUTES)
    public void scrapeRoleplayForum() {
        log.info("Starting Wakfu Roleplay scrape…");
        String uri = wakfuProperties.getForumUrl() + wakfuProperties.getForumRoleplayPath();
        log.info("Target URI: {}", uri);
        String html = playwrightManager.fetchHtml(uri);
        List<Topic> topics = CategoryParser.parse(html);

        // Do all that in parallel threads
        Topic rumors = findTopicById(topics, ID_TOPIC_RUMORS); // Could be a useCase, find topic then -> are replies > past -> do the communication pipeline
        Topic boards = findTopicById(topics, ID_TOPIC_BOARDS); // Could be a useCase, find topic then -> are replies > past -> do the communication pipeline
        long newestTopicId = topics.stream()
                .filter(topic -> !topic.pinned())
                .mapToLong(Topic::id)
                .max()
                .orElseThrow();
    }

    private Topic findTopicById(List<Topic> topics, long id) {
        return topics.stream()
                .filter(topic -> topic.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Topic with id " + id + " not found"));
    }
}
