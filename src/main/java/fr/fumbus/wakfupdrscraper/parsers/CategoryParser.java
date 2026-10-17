package fr.fumbus.wakfupdrscraper.parsers;

import fr.fumbus.wakfupdrscraper.models.Topic;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@UtilityClass
public class CategoryParser {

    private static final String TOPIC_ROW = "tr:has(a.ak-title-topic)";
    private static final String TOPIC_LINK = "a.ak-title-topic";
    private static final String TOPIC_PINNED_CLASS = "ak-pinned-topic";
    private static final int REPLIES_COLUMN = 1;
    private static final Pattern TOPIC_ID = Pattern.compile("/(\\d+)-[^/?#]*$");

    public static List<Topic> parse(String html) {
        List<Topic> topics = Jsoup.parse(html).select(TOPIC_ROW).stream()
                .map(CategoryParser::toTopic)
                .toList();
        if (topics.isEmpty()) {
            throw new IllegalStateException("No topic row found, the forum layout changed or a block page was served");
        }
        return topics;
    }

    private static Topic toTopic(Element row) {
        Element link = row.selectFirst(TOPIC_LINK);
        if(isNull(link)) {
            throw new IllegalStateException("No topic link found in row: " + row);
        }
        String href = link.attr("href");
        return new Topic(getTopicIdentifierFromLink(href), link.text(), URI.create(href), replyCount(row), isPinned(row));
    }

    private static boolean isPinned(Element row) {
        return row.hasClass(TOPIC_PINNED_CLASS);
    }

    private static long getTopicIdentifierFromLink(String href) {
        Matcher matcher = TOPIC_ID.matcher(href);
        if (!matcher.find()) {
            throw new IllegalStateException("No topic id in " + href);
        }
        return Long.parseLong(matcher.group(1));
    }

    private static int replyCount(Element row) {
        return Integer.parseInt(row.select("td").get(REPLIES_COLUMN).text().replaceAll("\\D", ""));
    }
}
