package fr.fumbus.wakfupdrscraper.parsers;

import fr.fumbus.wakfupdrscraper.playwright.parsers.TopicParser;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopicParserTest {
    private static final URI URL = URI.create("https://www.wakfu.com/fr/forum/584-general/422556-topic");

    @Test
    void readsOpeningPostAndRepliesFromTheSavedForumPage() throws Exception {
        try (var files = Files.list(Path.of("src/test/resources/fixtures"))) {
            String html = Files.readString(files.filter(path -> path.getFileName().toString().startsWith("[GLOBAL]"))
                    .findFirst().orElseThrow());
            var posts = TopicParser.parse(html, URL);
            assertThat(posts).hasSize(6);
            assertThat(posts).filteredOn(post -> post.id() == 3509780L).singleElement().satisfies(post -> {
                assertThat(post.authorHandle()).isEqualTo("Unkar#3113");
                assertThat(post.authorNickname()).isEqualTo("Unkar");
                assertThat(post.postedAt()).isEqualTo(Instant.parse("2020-08-02T17:49:57Z"));
                assertThat(post.content()).contains("affiche");
            });
        }
    }

    @Test
    void rejectsBlockPages() {
        assertThatThrownBy(() -> TopicParser.parse("<html>Access denied</html>", URL))
                .isInstanceOf(IllegalStateException.class);
    }
}
