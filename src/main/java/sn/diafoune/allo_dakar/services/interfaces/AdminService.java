package sn.diafoune.allo_dakar.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.diafoune.allo_dakar.web.dtos.booking.BookingResponse;
import sn.diafoune.allo_dakar.web.dtos.payment.PaymentResponse;
import sn.diafoune.allo_dakar.web.dtos.trip.TripResponse;
import sn.diafoune.allo_dakar.web.dtos.user.UserResponse;

import java.util.UUID;

/**
 * Opérations d'administration qui orchestrent d'autres services plutôt que de dupliquer leur
 * logique métier (ex. suspend/activate délèguent à UserService, qui journalise dans AuditLog).
 * Les listes ci-dessous sont volontairement sans filtre (vue globale réservée à ADMIN) —
 * contrairement à TripService.search ou BookingService.listMine qui restreignent la portée.
 */
public interface AdminService {

    Page<UserResponse> listUsers(Pageable pageable);

    void suspendUser(UUID adminId, UUID userId, String reason);

    void activateUser(UUID adminId, UUID userId);

    Page<TripResponse> listAllTrips(Pageable pageable);

    Page<BookingResponse> listAllBookings(Pageable pageable);

    Page<PaymentResponse> listAllPayments(Pageable pageable);
}
