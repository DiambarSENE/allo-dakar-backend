package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.enums.TripStatus;
import sn.diafoune.allo_dakar.exceptions.BookingException;
import sn.diafoune.allo_dakar.exceptions.ErrorCode;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.repositories.TripRepository;

import java.util.UUID;

/**
 * Isole la décrémentation de Trip.availableSeats dans sa PROPRE transaction
 * (REQUIRES_NEW) pour que chaque tentative de retry (voir BookingServiceImpl) reparte d'un
 * EntityManager propre, sans persistence context corrompu par un précédent conflit de version.
 *
 * Stratégie de concurrence retenue (Phase 1 §6, §33) : verrouillage OPTIMISTE via @Version sur
 * Trip, avec retry applicatif borné plutôt que PESSIMISTIC_WRITE — évite de bloquer les lectures
 * concurrentes sur un trajet à fort trafic. Une contrainte CHECK (available_seats >= 0) en base
 * (voir migration V8) sert de filet de sécurité supplémentaire indépendant de la couche
 * applicative.
 */
@Component
@RequiredArgsConstructor
public class SeatReservationService {

    private final TripRepository tripRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Trip reserveSeats(UUID tripId, int requestedSeats) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> ResourceNotFoundException.of("Trip", tripId));

        if (trip.getStatus() != TripStatus.PUBLISHED) {
            throw new BookingException(ErrorCode.BOOKING_NOT_AVAILABLE,
                    "Ce trajet n'est plus disponible à la réservation (statut : " + trip.getStatus() + ")");
        }
        if (trip.getAvailableSeats() < requestedSeats) {
            throw new BookingException(ErrorCode.BOOKING_SEATS_UNAVAILABLE,
                    "Places insuffisantes : " + trip.getAvailableSeats() + " disponible(s) pour " + requestedSeats + " demandée(s)");
        }

        trip.setAvailableSeats(trip.getAvailableSeats() - requestedSeats);
        if (trip.getAvailableSeats() == 0) {
            trip.setStatus(TripStatus.FULL);
        }

        // save() déclenche la vérification de version à l'écriture (flush/commit de cette
        // transaction REQUIRES_NEW) : en cas de conflit, ObjectOptimisticLockingFailureException
        // remonte immédiatement à l'appelant (BookingServiceImpl), qui retente ou abandonne.
        return tripRepository.save(trip);
    }

    /** Libère des places suite à une annulation (règle §48-9). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseSeats(UUID tripId, int seats) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> ResourceNotFoundException.of("Trip", tripId));

        trip.setAvailableSeats(Math.min(trip.getAvailableSeats() + seats, trip.getTotalSeats()));
        if (trip.getStatus() == TripStatus.FULL && trip.getAvailableSeats() > 0) {
            trip.setStatus(TripStatus.PUBLISHED);
        }
        tripRepository.save(trip);
    }
}
