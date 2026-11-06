package fr.fumbus.wakfupdrscraper.persistence.repositories.jpa;

import fr.fumbus.wakfupdrscraper.persistence.configurations.PostgresTestConfiguration;
import fr.fumbus.wakfupdrscraper.models.*;
import fr.fumbus.wakfupdrscraper.persistence.entities.*;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Limit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestConfiguration.class)
@Transactional
class PersistenceIT {

    private static final Instant NOW = Instant.parse("2026-11-06T12:00:00Z");

    @Autowired private TopicRepository topics;
    @Autowired private PostRepository posts;
    @Autowired private AuthorRepository authors;
    @Autowired private NotificationRepository notifications;
    @Autowired private EntityManager entityManager;

    @Test
    void forumIdentifiersSurvivePersistenceAndReload() {
        PostEntity post = persistPost();

        assertThat(post.isNew()).isFalse();
        assertThat(post.getTopic().isNew()).isFalse();
        entityManager.clear();

        PostEntity reloaded = posts.findById(123L).orElseThrow();
        assertThat(reloaded.getId()).isEqualTo(123L);
        assertThat(reloaded.getTopic().getId()).isEqualTo(456L);
        assertThat(reloaded.isNew()).isFalse();
        assertThat(reloaded.getTopic().isNew()).isFalse();

        reloaded.updateContent("hello");
        posts.saveAndFlush(reloaded);
        entityManager.clear();

        PostEntity edited = posts.findById(123L).orElseThrow();
        assertThat(edited.getContent()).isEqualTo("hello");
        assertThat(edited.getContentHash())
                .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
    }

    @Test
    void pendingQueryLimitsResultsAndFetchesTheDeliveryGraph() {
        PostEntity post = persistPost();
        NotificationEntity first = notifications.saveAndFlush(NotificationEntity.pending(
                post, NotificationType.NEW_POST, NotificationChannel.GLOBAL, NOW));
        notifications.saveAndFlush(NotificationEntity.pending(
                post, NotificationType.NEW_POST, NotificationChannel.BOARD, NOW.plusSeconds(1)));
        NotificationEntity sent = NotificationEntity.pending(
                post, NotificationType.NEW_POST, NotificationChannel.RUMORS, NOW.minusSeconds(1));
        sent.markSent("discord-message", NOW);
        notifications.saveAndFlush(sent);
        entityManager.clear();

        Set<NotificationEntity> pending = notifications.findByStatusOrderByCreatedAtAsc(
                NotificationStatus.PENDING, Limit.of(1));

        assertThat(pending).hasSize(1);
        NotificationEntity loaded = pending.iterator().next();
        assertThat(loaded.getId()).isEqualTo(first.getId());
        assertThat(loaded.getAttempts()).isZero();
        assertThat(Hibernate.isInitialized(loaded.getPostEntity())).isTrue();
        assertThat(Hibernate.isInitialized(loaded.getPostEntity().getTopic())).isTrue();
        assertThat(Hibernate.isInitialized(loaded.getPostEntity().getAuthorEntity())).isTrue();
    }

    private PostEntity persistPost() {
        TopicEntity topic = topics.saveAndFlush(new TopicEntity(new Topic(456L, "Topic",
                URI.create("https://www.wakfu.com/fr/forum/456-topic"), 0, false), NOW));
        Post scraped = new Post(123L, "Author#1234", "Author",
                URI.create("https://www.wakfu.com/profile/author"), NOW, "Original content");
        AuthorEntity author = authors.saveAndFlush(new AuthorEntity(scraped, NOW));
        return posts.saveAndFlush(new PostEntity(scraped, topic, author, NOW));
    }
}
