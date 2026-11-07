package fr.fumbus.wakfupdrscraper.playwright;

import fr.fumbus.wakfupdrscraper.models.Post;
import fr.fumbus.wakfupdrscraper.models.Topic;
import fr.fumbus.wakfupdrscraper.models.properties.ScraperProperties;
import fr.fumbus.wakfupdrscraper.playwright.parsers.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.ToLongFunction;

@Log4j2
@Component
@RequiredArgsConstructor
public class ForumScraper {
    private final PlaywrightManager playwrightManager;
    private final ScraperProperties properties;

    public Set<Topic> scrapeCategory(URI url) {
        return playwrightManager.withSession(fetcher -> scrapeCategory(url, fetcher));
    }

    Set<Topic> scrapeCategory(URI url, HtmlFetcher fetcher) {
        return new LinkedHashSet<>(crawl(url, fetcher, CategoryParser::parse, Topic::id,
                properties.getCategoryPageLimit(), false));
    }

    public List<Post> scrapeTopic(URI url) {
        return playwrightManager.withSession(fetcher -> scrapeTopic(url, fetcher));
    }

    List<Post> scrapeTopic(URI url, HtmlFetcher fetcher) {
        return crawl(ForumUrls.withoutQuery(url), fetcher, TopicParser::parse, Post::id,
                properties.getTopicPageLimit(), true);
    }

    private <T> List<T> crawl(URI start, HtmlFetcher fetcher, BiFunction<String, URI, ? extends Collection<T>> parser,
                            ToLongFunction<T> id, int pageLimit, boolean requireComplete) {
        if (pageLimit < 1) {
            throw new IllegalArgumentException("Page limit must be positive");
        }
        Map<Long, T> results = new LinkedHashMap<>();
        Set<URI> visited = new HashSet<>();
        URI current = ForumUrls.resolve(null, start.toString());
        while (true) {
            if (!visited.add(current)) {
                throw new IllegalStateException("Pagination loop at " + current);
            }
            String html = fetcher.fetch(current.toString());
            for (T item : parser.apply(html, current)) {
                results.put(id.applyAsLong(item), item);
            }
            Optional<URI> next = PaginationParser.nextPage(html, current);
            if (next.isEmpty()) {
                return List.copyOf(results.values());
            }
            URI nextUrl = next.get();
            if (!Objects.equals(start.getScheme(), nextUrl.getScheme())
                    || !Objects.equals(start.getAuthority(), nextUrl.getAuthority())
                    || !Objects.equals(start.getPath(), nextUrl.getPath())) {
                throw new IllegalStateException("Pagination left the current forum resource: " + nextUrl);
            }
            if (visited.contains(nextUrl)) {
                throw new IllegalStateException("Pagination loop at " + nextUrl);
            }
            if (visited.size() >= pageLimit) {
                if (requireComplete) {
                    throw new IllegalStateException("Topic page limit reached before the end: " + start);
                }
                log.warn("Category page limit {} reached at {}; older pages remain", pageLimit, current);
                return List.copyOf(results.values());
            }
            current = nextUrl;
        }
    }
}
