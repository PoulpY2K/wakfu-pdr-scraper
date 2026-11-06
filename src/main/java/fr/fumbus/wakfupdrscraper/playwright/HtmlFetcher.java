package fr.fumbus.wakfupdrscraper.playwright;

@FunctionalInterface
public interface HtmlFetcher {
    String fetch(String url);
}
