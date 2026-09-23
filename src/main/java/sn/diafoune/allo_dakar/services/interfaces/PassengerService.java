package sn.diafoune.allo_dakar.services.interfaces;

import sn.diafoune.allo_dakar.entities.PassengerProfile;
import sn.diafoune.allo_dakar.entities.User;

/**
 * Le profil passager est créé paresseusement (à la première réservation ou explicitement),
 * plutôt que systématiquement à l'inscription — un utilisateur peut n'être que conducteur.
 */
public interface PassengerService {

    PassengerProfile getOrCreateForUser(User user);

    void recalculateAfterCompletedTrip(java.util.UUID passengerUserId);
}
