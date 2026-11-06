package fr.fumbus.wakfupdrscraper.persistence.entities;

import fr.fumbus.wakfupdrscraper.models.NotificationChannel;
import fr.fumbus.wakfupdrscraper.models.NotificationStatus;
import fr.fumbus.wakfupdrscraper.models.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEntityTest {

    @Test
    void pendingNotificationStartsWithNoAttempts() {
        NotificationEntity notification = pendingNotification();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getAttempts()).isZero();
    }

    @Test
    void failuresAreCountedUntilTheRetryLimitIsReached() {
        NotificationEntity notification = pendingNotification();

        notification.recordFailure("Temporary failure", 2);

        assertThat(notification.getAttempts()).isEqualTo(1);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);

        notification.recordFailure("Still unavailable", 2);

        assertThat(notification.getAttempts()).isEqualTo(2);
        assertThat(notification.getLastError()).isEqualTo("Still unavailable");
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    private NotificationEntity pendingNotification() {
        return NotificationEntity.pending(new PostEntity(), NotificationType.NEW_POST,
                NotificationChannel.GLOBAL, Instant.parse("2026-11-06T12:00:00Z"));
    }
}
