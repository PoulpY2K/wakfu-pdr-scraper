package fr.fumbus.wakfupdrscraper.parsers;

import fr.fumbus.wakfupdrscraper.models.Topic;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryParserTest {

    private final Set<Topic> topics = CategoryParser.parse(categoryFixture());

    @Test
    void readsEveryTopicRowAndFlagsThePinnedOnes() {
        assertThat(topics).hasSize(50);
        assertThat(topics).filteredOn(Topic::pinned).hasSize(6);
    }

    @Test
    void readsTheRumorsThread() {
        Topic rumors = topicById(395506);

        assertThat(rumors.title()).isEqualTo("[RP] Rumeurs...");
        assertThat(rumors.replyCount()).isEqualTo(589);
        assertThat(rumors.pinned()).isTrue();
        assertThat(rumors.url()).hasToString("https://www.wakfu.com/fr/forum/584-general/395506-rp-rumeurs");
    }

    @Test
    void readsTheBoardThread() {
        Topic board = topicById(348768);

        assertThat(board.replyCount()).isEqualTo(816);
        assertThat(board.pinned()).isTrue();
    }

    @Test
    void readsTheNewestRegularTopic() {
        Topic newest = topics.stream()
                .filter(topic -> !topic.pinned())
                .max(Comparator.comparingLong(Topic::id))
                .orElseThrow();

        assertThat(newest.id()).isEqualTo(435756);
        assertThat(newest.title()).isEqualTo("[RP - Projet & Serveur Discord] WAKMART - Le supermarché participatif");
        assertThat(newest.replyCount()).isZero();
    }

    @Test
    void failsWhenThereIsNoTopicRow() {
        assertThatThrownBy(() -> CategoryParser.parse("<html><body>Access denied</body></html>"))
                .isInstanceOf(IllegalStateException.class);
    }

    private Topic topicById(long id) {
        return topics.stream().filter(topic -> topic.id() == id).findFirst().orElseThrow();
    }

    private static String categoryFixture() {
        try (Stream<Path> files = Files.list(Path.of("src/test/resources/fixtures"))) {
            Path fixture = files.filter(file -> file.getFileName().toString().startsWith("Histoire"))
                    .findFirst()
                    .orElseThrow();
            return Files.readString(fixture, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
