package fr.fumbus.wakfupdrscraper.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import fr.fumbus.wakfupdrscraper.models.properties.PlaywrightProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@Component
@RequiredArgsConstructor
public class PlaywrightManager {

    private static final List<String> CHROMIUM_ARGS = List.of("--disable-dev-shm-usage");
    private static final int CHALLENGE_TIMEOUT_MS = 30_000;

    private static final List<String> STEALTH_SCRIPTS = List.of(
            "stealth/navigator.webdriver.js",
            "stealth/chrome.runtime.js",
            "stealth/permissions.js");

    private final PlaywrightProperties properties;

    public String fetchHtml(String url) {
        try (Playwright playwright = Playwright.create();
             Browser browser = launchBrowser(playwright);
             BrowserContext context = browser.newContext(new Browser.NewContextOptions().setUserAgent(randomUserAgent()))) {
            STEALTH_SCRIPTS.forEach(script -> context.addInitScript(readScript(script)));
            return loadPage(context.newPage(), url);
        }
    }

    private Browser launchBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(properties.getHeadless())
                .setArgs(CHROMIUM_ARGS));
    }

    private String loadPage(Page page, String url) {
        Response response = page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        if (response == null || !response.ok()) {
            throw new IllegalStateException("Could not load " + url + ", status: " + (response == null ? "none" : response.status()));
        }
        waitForWafChallenge(page);
        return page.content();
    }

    private void waitForWafChallenge(Page page) {
        page.waitForFunction("() => !document.getElementById('challenge-container')",
                null, new Page.WaitForFunctionOptions().setTimeout(CHALLENGE_TIMEOUT_MS));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private String randomUserAgent() {
        List<String> userAgents = List.copyOf(properties.getUserAgents());
        String userAgent = userAgents.get(ThreadLocalRandom.current().nextInt(userAgents.size()));
        log.debug("User agent: {}", userAgent);
        return userAgent;
    }

    private String readScript(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + path, e);
        }
    }
}

