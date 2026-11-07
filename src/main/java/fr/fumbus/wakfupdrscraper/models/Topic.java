package fr.fumbus.wakfupdrscraper.models;

import java.net.URI;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
public record Topic(long id, String title, URI url, int replyCount, boolean pinned) {
}
