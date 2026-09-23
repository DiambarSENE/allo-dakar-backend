package sn.diafoune.allo_dakar.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.diafoune.allo_dakar.web.dtos.booking.BookingResponse;
import sn.diafoune.allo_dakar.web.dtos.booking.CreateBookingRequest;

import java.util.UUID;

public interface BookingService {

    /**
     * Décrémente Trip.availableSeats de façon atomique (verrouillage optimiste + retry borné —
     * voir BookingServiceImpl et Phase 1 §6). Ne confirme PAS la réservation : le statut reste
     * PENDING tant qu'aucun Payment SUCCESS n'est rattaché (règle §48-3).
     */
    BookingResponse create(UUID passengerUserId, CreateBookingRequest request);

    BookingResponse getById(UUID bookingId, UUID requestingUserId);

    /** §43 : consultation par le service client/admin, sans restriction de propriétaire (contrairement à getById). */
    BookingResponse getByIdForStaff(UUID bookingId);

    Page<BookingResponse> listMine(UUID passengerUserId, Pageable pageable);

    Page<BookingResponse> listForDriver(UUID driverUserId, Pageable pageable);

    /** Appelé en interne par PaymentService quand un Payment passe à SUCCESS. */
    BookingResponse confirmAfterPayment(UUID bookingId);

    BookingResponse cancel(UUID bookingId, UUID requestingUserId, String reason);

    /**
     * Annulation effectuée par un membre du service client ou un admin, pour le compte d'un
     * utilisateur (§43 "aider à traiter certaines annulations") — contrairement à cancel(), ne
     * vérifie PAS que l'appelant est le passager/conducteur du trajet, mais exige un motif
     * (traçabilité) et journalise l'action dans AuditLog.
     */
    BookingResponse cancelOnBehalf(UUID bookingId, UUID staffUserId, String reason);

    void markCompleted(UUID bookingId);
}
