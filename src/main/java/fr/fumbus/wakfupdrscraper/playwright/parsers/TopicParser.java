package fr.fumbus.wakfupdrscraper.playwright.parsers;

import fr.fumbus.wakfupdrscraper.models.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class TopicParser {
    private static final ZoneId FORUM_ZONE = ZoneId.of("Europe/Paris");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu - HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter OPENING_DATE = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendPattern("dd MMMM uuuu - HH:mm:ss").toFormatter(Locale.FRENCH).withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern DATE_TEXT = Pattern.compile("\\d{2} \\p{L}+ \\d{4} - \\d{2}:\\d{2}:\\d{2}");

    private TopicParser() {}

    public static List<Post> parse(String html, URI pageUrl) {
        Document document = Jsoup.parse(html);
        List<Post> posts = document.select(".ak-first-post[id^=entry], .ak-post[id^=entry]").stream()
                .map(element -> parsePost(element, document, pageUrl)).toList();
        if (posts.isEmpty()) {
            throw new IllegalStateException("No post found at " + pageUrl + "; layout changed or a block page was served");
        }
        return posts;
    }

    private static Post parsePost(Element element, Document document, URI pageUrl) {
        boolean opening = element.hasClass("ak-first-post");
        Element author = required(opening ? document : element,
                opening ? ".ak-subtitle .ak-author-nickname" : ".ak-post-infos .ak-author-nickname");
        String handle = author.text();
        Element nickname = author.clone();
        nickname.select(".ak-nickname-tag").remove();
        LocalDateTime postedAt;
        if (opening) {
            var matcher = DATE_TEXT.matcher(required(document, ".ak-subtitle").text());
            if (!matcher.find()) {
                throw new IllegalStateException("No opening post date at " + pageUrl);
            }
            postedAt = LocalDateTime.parse(matcher.group(), OPENING_DATE);
        } else {
            postedAt = LocalDateTime.parse(required(element, ".ak-post-infos .ak-date").attr("title"), DATE);
        }
        return new Post(Long.parseLong(element.id().substring("entry".length())), handle, nickname.text(),
                ForumUrls.resolve(pageUrl, author.attr("href")), postedAt.atZone(FORUM_ZONE).toInstant(),
                required(element, ".ak-text").html());
    }

    private static Element required(Element root, String selector) {
        Element element = root.selectFirst(selector);
        if (element == null) {
            throw new IllegalStateException("Missing forum post field: " + selector);
        }
        return element;
    }
}
