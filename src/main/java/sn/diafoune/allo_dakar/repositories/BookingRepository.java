package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.Booking;
import sn.diafoune.allo_dakar.entities.enums.BookingStatus;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findByPassengerId(UUID passengerId, Pageable pageable);

    /** Trip.driver est un DriverProfile, pas un User — on traverse jusqu'à l'utilisateur propriétaire du profil (utile avec un userId, ex: SecurityUtils.currentUserId()). */
    Page<Booking> findByTripDriverUserId(UUID userId, Pageable pageable);

    Optional<Booking> findByBookingReference(String bookingReference);

    boolean existsByTripIdAndPassengerIdAndStatusIn(UUID tripId, UUID passengerId, java.util.Collection<BookingStatus> statuses);
}
