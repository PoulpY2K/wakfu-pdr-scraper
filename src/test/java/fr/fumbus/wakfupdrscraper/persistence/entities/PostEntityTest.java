package fr.fumbus.wakfupdrscraper.persistence.entities;

import fr.fumbus.wakfupdrscraper.models.Post;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PostEntityTest {

    @Test
    void editingContentUpdatesItsHash() {
        PostEntity post = post();
        String originalHash = post.getContentHash();

        post.updateContent("hello");

        assertThat(post.getContent()).isEqualTo("hello");
        assertThat(post.getContentHash()).isNotEqualTo(originalHash)
                .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
    }

    @Test
    void invalidEditLeavesContentAndHashUnchanged() {
        PostEntity post = post();
        String originalHash = post.getContentHash();

        assertThatNullPointerException().isThrownBy(() -> post.updateContent(null));

        assertThat(post.getContent()).isEqualTo("Original content");
        assertThat(post.getContentHash()).isEqualTo(originalHash);
    }

    private PostEntity post() {
        Instant now = Instant.parse("2026-11-06T12:00:00Z");
        Post scraped = new Post(123L, "Author#1234", "Author",
                URI.create("https://www.wakfu.com/profile/author"), now, "Original content");
        return new PostEntity(scraped, null, null, now);
    }
}
