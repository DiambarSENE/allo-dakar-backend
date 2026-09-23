package sn.diafoune.allo_dakar.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.diafoune.allo_dakar.commons.utils.PaginationUtils;
import sn.diafoune.allo_dakar.commons.utils.SecurityUtils;
import sn.diafoune.allo_dakar.services.interfaces.TripService;
import sn.diafoune.allo_dakar.web.dtos.booking.CancelBookingRequest;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.common.PagedResponse;
import sn.diafoune.allo_dakar.web.dtos.trip.CreateTripRequest;
import sn.diafoune.allo_dakar.web.dtos.trip.TripResponse;
import sn.diafoune.allo_dakar.web.dtos.trip.TripSearchRequest;
import sn.diafoune.allo_dakar.web.dtos.trip.UpdateTripRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * GET /search et GET /{id} sont publics (voir SecurityConfig.PUBLIC_GET_ENDPOINTS) — un visiteur
 * non authentifié doit pouvoir consulter les trajets disponibles avant de créer un compte.
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<TripResponse>> create(@Valid @RequestBody CreateTripRequest request) {
        TripResponse response = tripService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Trajet créé (brouillon)", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DRIVER')")
    public ApiResponse<TripResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateTripRequest request) {
        return ApiResponse.of("Trajet mis à jour", tripService.update(SecurityUtils.currentUserId(), id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TripResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of("Trajet récupéré", tripService.getById(id));
    }

    @GetMapping("/search")
    public PagedResponse<TripResponse> search(
            @RequestParam(required = false) String departureCity,
            @RequestParam(required = false) String destinationCity,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minAvailableSeats,
            @RequestParam(required = false) UUID driver,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        TripSearchRequest criteria = new TripSearchRequest(
                departureCity, destinationCity, departureDate, minPrice, maxPrice, minAvailableSeats, driver);
        return PagedResponse.of("Trajets trouvés",
                tripService.search(criteria, PaginationUtils.of(page, size, sort, "asc")));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('DRIVER')")
    public ApiResponse<TripResponse> publish(@PathVariable UUID id) {
        return ApiResponse.of("Trajet publié", tripService.publish(SecurityUtils.currentUserId(), id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('DRIVER')")
    public ApiResponse<TripResponse> cancel(@PathVariable UUID id, @RequestBody(required = false) CancelBookingRequest request) {
        String reason = request != null ? request.reason() : null;
        return ApiResponse.of("Trajet annulé", tripService.cancel(SecurityUtils.currentUserId(), id, reason));
    }
}
