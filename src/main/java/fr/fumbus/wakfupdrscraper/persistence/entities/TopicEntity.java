package fr.fumbus.wakfupdrscraper.persistence.entities;

import fr.fumbus.wakfupdrscraper.models.Topic;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@Getter
@Setter
@Entity
@Table(name = "topic")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicEntity extends ForumEntity {
    @NotNull
    @Column(name = "title", nullable = false, length = Integer.MAX_VALUE)
    private String title;

    @NotNull
    @Column(name = "url", nullable = false, length = Integer.MAX_VALUE)
    private String url;

    @NotNull
    @Column(name = "pinned", nullable = false)
    private Boolean pinned;

    @NotNull
    @Column(name = "reply_count", nullable = false)
    private Integer replyCount;

    @NotNull
    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @NotNull
    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AuthorEntity authorEntity;

    public TopicEntity(Topic topic, Instant now) {
        super(topic.id());
        this.firstSeenAt = now;
        refresh(topic, now);
    }

    public void refresh(Topic topic, Instant now) {
        this.title = topic.title();
        this.url = topic.url().toString();
        this.pinned = topic.pinned();
        this.replyCount = topic.replyCount();
        this.lastSeenAt = now;
    }
}