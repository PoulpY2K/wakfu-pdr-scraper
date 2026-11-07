package fr.fumbus.wakfupdrscraper.playwright;

import com.microsoft.playwright.*;
import fr.fumbus.wakfupdrscraper.models.properties.PlaywrightProperties;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlaywrightManagerTest {
    private static final String URL = "https://www.wakfu.com/fr/forum/123-topic";

    @Test
    void allowsAnHttpErrorChallengeToNavigateToAValidForumPage() {
        Page page = mock(Page.class);
        Locator challenge = mock(Locator.class);
        Response blocked = mock(Response.class);
        Response recovered = mock(Response.class);
        Request request = mock(Request.class);
        Frame mainFrame = mock(Frame.class);
        when(page.mainFrame()).thenReturn(mainFrame);
        when(recovered.frame()).thenReturn(mainFrame);
        when(page.navigate(eq(URL), any(Page.NavigateOptions.class))).thenReturn(blocked);
        when(page.locator("#challenge-container")).thenReturn(challenge);
        when(challenge.count()).thenReturn(1);
        when(recovered.request()).thenReturn(request);
        when(request.isNavigationRequest()).thenReturn(true);
        when(recovered.ok()).thenReturn(true);
        when(page.content()).thenReturn("<div class='ak-posts-list'>Forum</div>");
        AtomicReference<Consumer<Response>> listener = new AtomicReference<>();
        doAnswer(invocation -> { listener.set(invocation.getArgument(0)); return null; })
                .when(page).onResponse(any());
        when(page.waitForFunction(anyString(), isNull(), any(Page.WaitForFunctionOptions.class)))
                .thenAnswer(invocation -> { listener.get().accept(recovered); return null; });

        assertThat(new PlaywrightManager(new PlaywrightProperties()).loadPage(page, URL)).contains("Forum");
    }

    @Test
    void doesNotTreatAnIframeResponseAsChallengeRecovery() {
        Page page = mock(Page.class);
        Locator challenge = mock(Locator.class);
        Response blocked = mock(Response.class);
        Response iframe = mock(Response.class);
        Request request = mock(Request.class);
        when(page.mainFrame()).thenReturn(mock(Frame.class));
        when(iframe.frame()).thenReturn(mock(Frame.class));
        when(iframe.request()).thenReturn(request);
        when(request.isNavigationRequest()).thenReturn(true);
        when(page.navigate(eq(URL), any(Page.NavigateOptions.class))).thenReturn(blocked);
        when(blocked.status()).thenReturn(403);
        when(page.locator("#challenge-container")).thenReturn(challenge);
        when(challenge.count()).thenReturn(1);
        AtomicReference<Consumer<Response>> listener = new AtomicReference<>();
        doAnswer(invocation -> { listener.set(invocation.getArgument(0)); return null; })
                .when(page).onResponse(any());
        when(page.waitForFunction(anyString(), isNull(), any(Page.WaitForFunctionOptions.class)))
                .thenAnswer(invocation -> { listener.get().accept(iframe); return null; });

        assertThatThrownBy(() -> new PlaywrightManager(new PlaywrightProperties()).loadPage(page, URL))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("403");
        verify(page).offResponse(any());
    }

    @Test
    void rejectsAnHttpErrorWithoutAChallenge() {
        Page page = mock(Page.class);
        Locator challenge = mock(Locator.class);
        Response missing = mock(Response.class);
        when(page.navigate(eq(URL), any(Page.NavigateOptions.class))).thenReturn(missing);
        when(page.locator("#challenge-container")).thenReturn(challenge);
        when(missing.status()).thenReturn(404);

        assertThatThrownBy(() -> new PlaywrightManager(new PlaywrightProperties()).loadPage(page, URL))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("404");
    }
}
