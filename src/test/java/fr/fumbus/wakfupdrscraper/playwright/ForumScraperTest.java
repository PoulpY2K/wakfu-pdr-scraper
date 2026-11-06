package fr.fumbus.wakfupdrscraper.playwright;

import fr.fumbus.wakfupdrscraper.models.properties.ScrapingProperties;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class ForumScraperTest {
    private static final URI URL = URI.create("https://www.wakfu.com/fr/forum/123-category");

    @Test
    void followsCategoryPagesAndDeduplicatesByForumId() {
        var pages = Map.of(URL.toString(), category("Original", "?page=2"),
                URL + "?page=2", category("Renamed", null));
        var scraper = new ForumScraper(null, new ScrapingProperties());
        var topics = scraper.scrapeCategory(URL, uri -> pages.get(uri));
        assertThat(topics).singleElement().satisfies(topic -> {
            assertThat(topic.id()).isEqualTo(456L);
            assertThat(topic.url()).isEqualTo(URI.create("https://www.wakfu.com/fr/forum/456-topic"));
        });
    }

    @Test
    void honorsConfiguredCategoryDepth() {
        var properties = new ScrapingProperties();
        properties.setCategoryPageLimit(1);
        var fetched = new ArrayList<String>();
        new ForumScraper(null, properties).scrapeCategory(URL, uri -> {
            fetched.add(uri);
            return category("Topic", "?page=2");
        });
        assertThat(fetched).containsExactly(URL.toString());
    }

    @Test
    void rejectsPaginationLoops() {
        assertThatThrownBy(() -> new ForumScraper(null, new ScrapingProperties())
                .scrapeCategory(URL, uri -> category("Topic", URL.toString())))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("loop");
    }

    @Test
    void followsEveryTopicPageAndUsesPostIdsForDeduplication() {
        var pages = Map.of(URL.toString(), post("Original") + next("?page=2"),
                URL + "?page=2", post("Edited"));
        var posts = new ForumScraper(null, new ScrapingProperties())
                .scrapeTopic(URI.create(URL + "?poid=456#entry456"), pages::get);
        assertThat(posts).singleElement().satisfies(post -> {
            assertThat(post.id()).isEqualTo(456);
            assertThat(post.content()).isEqualTo("Edited");
        });
    }

    @Test
    void refusesToReturnPartialTopicContentWhenTheLimitIsReached() {
        var properties = new ScrapingProperties();
        properties.setTopicPageLimit(1);
        assertThatThrownBy(() -> new ForumScraper(null, properties)
                .scrapeTopic(URL, uri -> post("Original") + next("?page=2")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("limit");
    }

    @Test
    void rejectsPaginationOutsideTheCurrentForumResource() {
        assertThatThrownBy(() -> new ForumScraper(null, new ScrapingProperties())
                .scrapeCategory(URL, uri -> category("Topic", "https://example.com/other")))
                .isInstanceOf(IllegalStateException.class);
    }

    private String category(String title, String next) {
        return "<table><tr><td><a class='ak-title-topic' href='/fr/forum/456-topic'>" + title
                + "</a></td><td>2</td></tr></table>"
                + (next == null ? "" : "<ul class='ak-pagination'><li><a href='" + next + "'>›</a></li></ul>");
    }

    private String next(String href) {
        return "<ul class='ak-pagination'><li><a href='" + href + "'>›</a></li></ul>";
    }

    private String post(String content) {
        return "<div class='ak-post' id='entry456'><div class='ak-post-infos'>"
                + "<a class='ak-author-nickname' href='https://account.ankama.com/profile/Author'>Author<span class='ak-nickname-tag'>#1234</span></a>"
                + "<span class='ak-date' title='06/11/2026 - 12:00:00'></span></div>"
                + "<div class='ak-text'>" + content + "</div></div>";
    }
}
