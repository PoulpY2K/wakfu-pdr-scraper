package fr.fumbus.wakfupdrscraper.persistence.entities;

import fr.fumbus.wakfupdrscraper.models.Post;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@Getter
@Setter
@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity extends ForumEntity {
    @NotNull
    @Column(name = "posted_at", nullable = false)
    private Instant postedAt;

    @NotNull
    @Setter(AccessLevel.NONE)
    @Column(name = "content", nullable = false, length = Integer.MAX_VALUE)
    private String content;

    @NotNull
    @Setter(AccessLevel.NONE)
    @Column(name = "content_hash", nullable = false, length = Integer.MAX_VALUE)
    private String contentHash;

    @NotNull
    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private TopicEntity topic;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private AuthorEntity authorEntity;

    public PostEntity(Post post, TopicEntity topic, AuthorEntity author, Instant now) {
        super(post.id());
        this.topic = topic;
        this.authorEntity = author;
        this.postedAt = post.postedAt();
        updateContent(post.content());
        this.firstSeenAt = now;
    }

    public void updateContent(String content) {
        String contentHash = sha256(Objects.requireNonNull(content, "content"));
        this.content = content;
        this.contentHash = contentHash;
    }

    private static String sha256(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
