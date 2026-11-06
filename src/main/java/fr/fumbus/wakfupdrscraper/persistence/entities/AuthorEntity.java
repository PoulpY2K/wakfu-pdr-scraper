package fr.fumbus.wakfupdrscraper.persistence.entities;

import fr.fumbus.wakfupdrscraper.models.Post;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@Getter
@Setter
@Entity
@Table(name = "author")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthorEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Column(name = "handle", nullable = false, length = Integer.MAX_VALUE)
    private String handle;

    @NotNull
    @Column(name = "nickname", nullable = false, length = Integer.MAX_VALUE)
    private String nickname;

    @NotNull
    @Column(name = "profile_url", nullable = false, length = Integer.MAX_VALUE)
    private String profileUrl;

    @NotNull
    @Column(name = "first_seen_at", nullable = false)
    private Instant  firstSeenAt;

    @OneToMany(mappedBy = "authorEntity")
    private Set<PostEntity> posts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "authorEntity")
    private Set<TopicEntity> topics = new LinkedHashSet<>();

    public AuthorEntity(Post post, Instant now) {
        this.handle = post.authorHandle();
        this.nickname = post.authorNickname();
        this.profileUrl = post.authorUrl().toString();
        this.firstSeenAt = now;
    }
}