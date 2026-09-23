package sn.diafoune.allo_dakar.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import sn.diafoune.allo_dakar.services.interfaces.BookingService;
import sn.diafoune.allo_dakar.web.dtos.booking.BookingResponse;
import sn.diafoune.allo_dakar.web.dtos.booking.CancelBookingRequest;
import sn.diafoune.allo_dakar.web.dtos.booking.CreateBookingRequest;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.common.PagedResponse;

import java.util.UUID;

/**
 * "confirm" n'est volontairement pas exposé ici : la confirmation se fait automatiquement quand
 * un Payment associé passe à SUCCESS (règle §48-3), via BookingService.confirmAfterPayment,
 * appelé en interne par PaymentServiceImpl — jamais directement par le client.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PASSENGER')")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> create(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Réservation créée", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BookingResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of("Réservation récupérée",
                bookingService.getById(id, SecurityUtils.currentUserId()));
    }

    @GetMapping("/me")
    public PagedResponse<BookingResponse> myBookings(@RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Réservations récupérées",
                bookingService.listMine(SecurityUtils.currentUserId(), PaginationUtils.of(page, size, null, null)));
    }

    /**
     * Réservations reçues par le conducteur connecté, tous trajets confondus (§27 "Réservations").
     * @PreAuthorize ici override le hasRole('PASSENGER') de la classe — nécessaire puisque ce
     * endpoint est le seul de ce contrôleur réservé à DRIVER plutôt que PASSENGER.
     */
    @GetMapping("/received")
    @PreAuthorize("hasRole('DRIVER')")
    public PagedResponse<BookingResponse> receivedByMe(@RequestParam(required = false) Integer page,
                                                          @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Réservations reçues récupérées",
                bookingService.listForDriver(SecurityUtils.currentUserId(), PaginationUtils.of(page, size, null, null)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BookingResponse> cancel(@PathVariable UUID id,
                                                 @RequestBody(required = false) CancelBookingRequest request) {
        String reason = request != null ? request.reason() : null;
        return ApiResponse.of("Réservation annulée",
                bookingService.cancel(id, SecurityUtils.currentUserId(), reason));
    }
}
