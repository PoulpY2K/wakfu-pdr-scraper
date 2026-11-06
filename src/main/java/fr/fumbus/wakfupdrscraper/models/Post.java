package fr.fumbus.wakfupdrscraper.models;

import java.net.URI;
import java.time.Instant;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
public record Post(long id, String authorHandle, String authorNickname, URI authorUrl, Instant postedAt, String content) {
}
