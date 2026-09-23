package sn.diafoune.allo_dakar.web.dtos.notification;

import sn.diafoune.allo_dakar.entities.enums.NotificationChannel;
import sn.diafoune.allo_dakar.entities.enums.NotificationStatus;
import sn.diafoune.allo_dakar.entities.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        NotificationChannel channel,
        String title,
        String message,
        NotificationStatus status,
        Instant sentAt,
        Instant readAt,
        Instant createdAt
) {
}
