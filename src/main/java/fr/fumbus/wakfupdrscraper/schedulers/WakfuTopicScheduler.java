package fr.fumbus.wakfupdrscraper.schedulers;

import fr.fumbus.wakfupdrscraper.models.Topic;
import fr.fumbus.wakfupdrscraper.models.exceptions.technical.TopicNotFoundException;
import fr.fumbus.wakfupdrscraper.models.properties.WakfuProperties;
import fr.fumbus.wakfupdrscraper.playwright.ForumScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.net.URI;
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

    private final ExecutorService executorService;
    private final ForumScraper forumScraper;
    private final WakfuProperties wakfuProperties;

    /**
     * Scrapes the Roleplay (RP) section of the Wakfu forum.
     */
    @Scheduled(fixedRateString = "${scheduler.rate:60}", timeUnit = TimeUnit.MINUTES)
    public void scrapeRoleplayForum() {
        log.info("Starting Wakfu Roleplay scrape…");
        String uri = wakfuProperties.getForumUrl() + wakfuProperties.getForumRoleplayPath();
        log.info("Target URI: {}", uri);
        Set<Topic> topics = forumScraper.scrapeCategory(URI.create(uri));

        // Do all that in parallel threads
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> processPinnedTopic(topics, ID_TOPIC_RUMORS), executorService),
                CompletableFuture.runAsync(() -> processPinnedTopic(topics, ID_TOPIC_BOARDS), executorService),
                CompletableFuture.runAsync(() -> processRegularTopics(topics), executorService)
        ).join();
    }

    // Ingestion must compare persisted post IDs and content hashes, not only reply counts.
    private void processPinnedTopic(Set<Topic> topics, Long topicId) {
        Topic topic = findTopicById(topics, topicId);
        log.info("[pinned] {}", topic);
    }

    private void processRegularTopics(Set<Topic> topics) {
        Set<Topic> regularTopics = findRegularTopics(topics);
        for (Topic topic : regularTopics) {
            log.info("[non-pinned] {}", topic);
        }
    }

    private Topic findTopicById(Set<Topic> topics, long id) {
        return topics.stream()
                .filter(topic -> topic.id() == id)
                .findFirst()
                .orElseThrow(() -> new TopicNotFoundException("Topic with id " + id + " not found"));
    }

    private Set<Topic> findRegularTopics(Set<Topic> topics) {
        return topics.stream()
                .filter(topic -> !topic.pinned())
                .collect(Collectors.toSet());
    }
}
