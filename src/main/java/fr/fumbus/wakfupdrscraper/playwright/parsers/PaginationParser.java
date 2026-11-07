package fr.fumbus.wakfupdrscraper.playwright.parsers;

import org.jsoup.Jsoup;
import java.net.URI;
import java.util.Optional;

public final class PaginationParser {
    private PaginationParser() {}

    public static Optional<URI> nextPage(String html, URI current) {
        return Jsoup.parse(html).select("ul.ak-pagination li:not(.disabled):not(.active) > a[href], a[rel=next][href]")
                .stream()
                .filter(link -> "›".equals(link.text()) || link.attr("rel").equals("next"))
                .filter(link -> !link.attr("href").isBlank() && !link.attr("href").startsWith("javascript:"))
                .map(link -> ForumUrls.resolve(current, link.attr("href")))
                .findFirst();
    }
}
