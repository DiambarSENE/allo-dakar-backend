package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.entities.Notification;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.NotificationChannel;
import sn.diafoune.allo_dakar.entities.enums.NotificationStatus;
import sn.diafoune.allo_dakar.entities.enums.NotificationType;
import sn.diafoune.allo_dakar.exceptions.ForbiddenException;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.repositories.NotificationRepository;
import sn.diafoune.allo_dakar.services.interfaces.NotificationService;

import java.time.Instant;
import java.util.UUID;

/**
 * Seul le canal IN_APP est réellement délivré (persisté + consultable via l'API). EMAIL/SMS/PUSH
 * ne sont pas intégrés tant qu'aucun fournisseur externe n'est spécifié (§22) — étendre ce
 * service avec un NotificationSender par canal le jour où ces intégrations seront spécifiées.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void notify(User recipient, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .channel(NotificationChannel.IN_APP)
                .title(title)
                .message(message)
                .status(NotificationStatus.SENT)
                .sentAt(Instant.now())
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public Page<Notification> listMine(UUID userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional
    public void markRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", notificationId));
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ForbiddenException("Cette notification ne vous appartient pas");
        }
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);
    }
}
