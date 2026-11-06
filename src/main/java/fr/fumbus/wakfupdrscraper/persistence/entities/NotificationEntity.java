package fr.fumbus.wakfupdrscraper.persistence.entities;

import fr.fumbus.wakfupdrscraper.models.NotificationChannel;
import fr.fumbus.wakfupdrscraper.models.NotificationStatus;
import fr.fumbus.wakfupdrscraper.models.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@Getter
@Setter
@Entity
@Table(name = "notification")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "type", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @NotNull
    @Column(name = "channel", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @NotNull
    @Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "discord_message_id", length = Integer.MAX_VALUE)
    private String discordMessageId;

    @Column(name = "last_error", length = Integer.MAX_VALUE)
    private String lastError;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity postEntity;

    public static NotificationEntity pending(PostEntity post, NotificationType type, NotificationChannel channel, Instant now) {
        NotificationEntity notification = new NotificationEntity();
        notification.postEntity = post;
        notification.type = type;
        notification.channel = channel;
        notification.status = NotificationStatus.PENDING;
        notification.createdAt = now;
        return notification;
    }

    public void markSent(String discordMessageId, Instant now) {
        this.status = NotificationStatus.SENT;
        this.discordMessageId = discordMessageId;
        this.sentAt = now;
    }

    public void recordFailure(String error, int maxAttempts) {
        this.attempts++;
        this.lastError = error;
        if (attempts >= maxAttempts) {
            this.status = NotificationStatus.FAILED;
        }
    }
}
