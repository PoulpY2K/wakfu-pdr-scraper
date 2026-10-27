package fr.fumbus.wakfupdrscraper.schedulers;

import fr.fumbus.wakfupdrscraper.models.Topic;
import fr.fumbus.wakfupdrscraper.models.exceptions.technical.TopicNotFoundException;
import fr.fumbus.wakfupdrscraper.models.properties.WakfuProperties;
import fr.fumbus.wakfupdrscraper.parsers.CategoryParser;
import fr.fumbus.wakfupdrscraper.playwright.PlaywrightManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    // ONLY FOR TESTING PURPOSES
    private static final long ID_TOPIC_NEWEST = 434898;

    private final ExecutorService executorService;
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
        Set<Topic> topics = CategoryParser.parse(html);

        // Do all that in parallel threads
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> processPinnedTopic(topics, ID_TOPIC_RUMORS), executorService),
                CompletableFuture.runAsync(() -> processPinnedTopic(topics, ID_TOPIC_BOARDS), executorService),
                CompletableFuture.runAsync(() -> processNewTopics(topics), executorService)
        ).join();
    }

    // Could be a useCase, find topic then -> are replies > past -> do the communication pipeline
    private void processPinnedTopic(Set<Topic> topics, Long topicId) {
        Topic topic = findTopicById(topics, topicId);
        log.info("[pinned] {}", topic);
    }

    // For each of those that have new content -> if just new and no replies do a new pipeline, if not, reuse the useCase of pinned topics
    private void processNewTopics(Set<Topic> topics) {
        Set<Topic> newestTopics = findNewestTopics(topics);
        for (Topic topic : newestTopics) {
            log.info("[non-pinned] {}", topic);
        }
    }

    private Topic findTopicById(Set<Topic> topics, long id) {
        return topics.stream()
                .filter(topic -> topic.id() == id)
                .findFirst()
                .orElseThrow(() -> new TopicNotFoundException("Topic with id " + id + " not found"));
    }

    private Set<Topic> findNewestTopics(Set<Topic> topics) {
        return topics.stream()
                .filter(topic -> !topic.pinned())
                .collect(Collectors.toSet());
    }
}
