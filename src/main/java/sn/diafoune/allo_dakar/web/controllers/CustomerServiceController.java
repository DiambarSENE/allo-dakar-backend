package sn.diafoune.allo_dakar.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.diafoune.allo_dakar.commons.utils.PaginationUtils;
import sn.diafoune.allo_dakar.commons.utils.SecurityUtils;
import sn.diafoune.allo_dakar.services.interfaces.AdminService;
import sn.diafoune.allo_dakar.services.interfaces.BookingService;
import sn.diafoune.allo_dakar.services.interfaces.DisputeService;
import sn.diafoune.allo_dakar.services.interfaces.UserService;
import sn.diafoune.allo_dakar.web.dtos.booking.BookingResponse;
import sn.diafoune.allo_dakar.web.dtos.booking.CancelBookingRequest;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.common.PagedResponse;
import sn.diafoune.allo_dakar.web.dtos.dispute.DisputeResponse;
import sn.diafoune.allo_dakar.web.dtos.trip.TripResponse;
import sn.diafoune.allo_dakar.web.dtos.user.UserResponse;

import java.util.UUID;

/**
 * Périmètre RÉEL du rôle CUSTOMER_SERVICE (§43) : consulter les réservations, rechercher un
 * utilisateur, consulter un trajet (déjà public via GET /api/v1/trips/{id}, rien à dupliquer
 * ici), aider à traiter certaines annulations, consulter les litiges. Volontairement séparé de
 * /api/v1/admin/** (réservé strictement à ADMIN) plutôt que d'ouvrir ce dernier et de reverrouiller
 * les endpoints sensibles un par un — périmètre minimal, décidé une fois, jamais à auditer
 * endpoint par endpoint (§66 : ne jamais donner au frontend plus de permissions que nécessaire).
 * La résolution des litiges et la suspension/activation des comptes restent ADMIN uniquement
 * (voir AdminController) — annuler une réservation reste l'unique action d'écriture accordée ici.
 */
@RestController
@RequestMapping("/api/v1/customer-service")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
public class CustomerServiceController {

    private final UserService userService;
    private final BookingService bookingService;
    private final AdminService adminService;
    private final DisputeService disputeService;

    @GetMapping("/users")
    public PagedResponse<UserResponse> searchUsers(@RequestParam(required = false) String search,
                                                     @RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Utilisateurs récupérés",
                userService.search(search, PaginationUtils.of(page, size, null, null)));
    }

    @GetMapping("/bookings")
    public PagedResponse<BookingResponse> bookings(@RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Réservations récupérées",
                adminService.listAllBookings(PaginationUtils.of(page, size, null, null)));
    }

    @GetMapping("/bookings/{id}")
    public ApiResponse<BookingResponse> booking(@PathVariable UUID id) {
        return ApiResponse.of("Réservation récupérée", bookingService.getByIdForStaff(id));
    }

    /** §43 "aider à traiter certaines annulations" — motif obligatoire, action journalisée (voir cancelOnBehalf). */
    @PostMapping("/bookings/{id}/cancel")
    public ApiResponse<BookingResponse> cancelBooking(@PathVariable UUID id, @Valid @RequestBody CancelBookingRequest request) {
        return ApiResponse.of("Réservation annulée par le service client",
                bookingService.cancelOnBehalf(id, SecurityUtils.currentUserId(), request.reason()));
    }

    @GetMapping("/trips")
    public PagedResponse<TripResponse> trips(@RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Trajets récupérés",
                adminService.listAllTrips(PaginationUtils.of(page, size, null, null)));
    }

    @GetMapping("/disputes")
    public PagedResponse<DisputeResponse> disputes(@RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Litiges récupérés",
                disputeService.listAll(PaginationUtils.of(page, size, null, null)));
    }

    @GetMapping("/disputes/{id}")
    public ApiResponse<DisputeResponse> dispute(@PathVariable UUID id) {
        return ApiResponse.of("Litige récupéré", disputeService.getById(id));
    }
}
