package fr.fumbus.wakfupdrscraper.parsers;

import fr.fumbus.wakfupdrscraper.playwright.parsers.PaginationParser;
import org.junit.jupiter.api.Test;
import java.net.URI;
import static org.assertj.core.api.Assertions.assertThat;

class PaginationParserTest {
    @Test
    void resolvesTheEnabledNextLink() {
        String html = "<ul class='ak-pagination'><li><a href='?page=1'>‹</a></li><li><a href='?page=3'>›</a></li></ul>";
        assertThat(PaginationParser.nextPage(html, URI.create("https://www.wakfu.com/fr/forum/123-topic?page=2")))
                .contains(URI.create("https://www.wakfu.com/fr/forum/123-topic?page=3"));
    }

    @Test
    void ignoresDisabledAndJavascriptLinks() {
        assertThat(PaginationParser.nextPage("<ul class='ak-pagination'><li class='disabled'><a href='javascript:void(0)'>›</a></li></ul>",
                URI.create("https://www.wakfu.com/fr/forum/123-topic"))).isEmpty();
    }
}
