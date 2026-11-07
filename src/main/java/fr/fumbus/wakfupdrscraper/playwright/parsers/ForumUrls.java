package fr.fumbus.wakfupdrscraper.playwright.parsers;

import java.net.URI;

public final class ForumUrls {
    private ForumUrls() {}

    public static URI resolve(URI base, String href) {
        // URI.resolve treats query-only references as a new relative path.
        URI resolved = href.startsWith("?") && base != null
                ? URI.create(withoutQuery(base).toString() + href)
                : base == null ? URI.create(href) : base.resolve(href);
        if (!resolved.isAbsolute() || !("https".equalsIgnoreCase(resolved.getScheme())
                || "http".equalsIgnoreCase(resolved.getScheme())) || resolved.getHost() == null) {
            throw new IllegalStateException("Not an absolute HTTP forum URL: " + resolved);
        }
        String value = resolved.toString();
        int fragment = value.indexOf('#');
        return URI.create(fragment < 0 ? value : value.substring(0, fragment)).normalize();
    }

    public static URI withoutQuery(URI uri) {
        String value = uri.toString().split("[?#]", 2)[0];
        return URI.create(value);
    }
}
