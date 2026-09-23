package sn.diafoune.allo_dakar.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.NotificationType;

/**
 * Créée par les autres services en réaction à un événement métier (réservation, paiement,
 * vérification conducteur, avis, signalement — voir §22). L'envoi effectif EMAIL/SMS/PUSH n'est
 * pas intégré tant qu'aucun fournisseur externe n'est spécifié ; seul IN_APP est persistant.
 */
public interface NotificationService {

    void notify(User recipient, NotificationType type, String title, String message);

    Page<sn.diafoune.allo_dakar.entities.Notification> listMine(java.util.UUID userId, Pageable pageable);

    void markRead(java.util.UUID notificationId, java.util.UUID userId);
}
