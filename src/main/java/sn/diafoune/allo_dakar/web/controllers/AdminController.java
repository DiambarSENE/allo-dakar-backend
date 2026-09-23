package sn.diafoune.allo_dakar.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import sn.diafoune.allo_dakar.services.interfaces.DisputeService;
import sn.diafoune.allo_dakar.services.interfaces.ReportService;
import sn.diafoune.allo_dakar.services.interfaces.ReviewService;
import sn.diafoune.allo_dakar.services.interfaces.VerificationService;
import sn.diafoune.allo_dakar.web.dtos.booking.BookingResponse;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.common.PagedResponse;
import sn.diafoune.allo_dakar.web.dtos.dispute.DisputeResponse;
import sn.diafoune.allo_dakar.web.dtos.dispute.ResolveDisputeRequest;
import sn.diafoune.allo_dakar.web.dtos.payment.PaymentResponse;
import sn.diafoune.allo_dakar.web.dtos.report.ReportResponse;
import sn.diafoune.allo_dakar.web.dtos.report.ResolveReportRequest;
import sn.diafoune.allo_dakar.web.dtos.trip.TripResponse;
import sn.diafoune.allo_dakar.web.dtos.user.SuspendUserRequest;
import sn.diafoune.allo_dakar.web.dtos.user.UserResponse;
import sn.diafoune.allo_dakar.web.dtos.verification.ReviewVerificationRequest;
import sn.diafoune.allo_dakar.web.dtos.verification.VerificationResponse;

import java.util.UUID;

/**
 * Toute cette famille d'endpoints est déjà restreinte à ROLE_ADMIN au niveau du filtre de
 * sécurité (SecurityConfig : "/api/v1/admin/**" -> hasAnyRole("ADMIN")) — pas besoin de
 * @PreAuthorize supplémentaire par méthode.
 *
 * NB : les actions de vérification portent sur l'ID de la soumission (DriverVerification), pas
 * sur l'ID du DriverProfile — un conducteur peut soumettre plusieurs documents. Léger écart
 * assumé par rapport au chemin indicatif du §49 ("/admin/drivers/{id}/verify"), documenté ici
 * plutôt que de casser le modèle déjà construit (VerificationService) — voir §64.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final VerificationService verificationService;
    private final ReportService reportService;
    private final ReviewService reviewService;
    private final DisputeService disputeService;

    @GetMapping("/users")
    public PagedResponse<UserResponse> listUsers(@RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Utilisateurs récupérés",
                adminService.listUsers(PaginationUtils.of(page, size, null, null)));
    }

    @PostMapping("/users/{id}/suspend")
    public ApiResponse<Void> suspendUser(@PathVariable UUID id, @Valid @RequestBody SuspendUserRequest request) {
        adminService.suspendUser(SecurityUtils.currentUserId(), id, request.reason());
        return ApiResponse.ok("Utilisateur suspendu");
    }

    @PostMapping("/users/{id}/activate")
    public ApiResponse<Void> activateUser(@PathVariable UUID id) {
        adminService.activateUser(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok("Utilisateur réactivé");
    }

    @GetMapping("/drivers/verifications")
    public PagedResponse<VerificationResponse> pendingVerifications(@RequestParam(required = false) Integer page,
                                                                       @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Vérifications en attente récupérées",
                verificationService.listPending(PaginationUtils.of(page, size, null, null)));
    }

    @PostMapping("/verifications/{id}/approve")
    public ApiResponse<VerificationResponse> approveVerification(@PathVariable UUID id,
                                                                    @RequestBody(required = false) ReviewVerificationRequest request) {
        ReviewVerificationRequest body = request != null ? request : new ReviewVerificationRequest(null, null);
        return ApiResponse.of("Conducteur vérifié",
                verificationService.approve(id, SecurityUtils.currentUserId(), body));
    }

    @PostMapping("/verifications/{id}/reject")
    public ApiResponse<VerificationResponse> rejectVerification(@PathVariable UUID id,
                                                                   @RequestBody(required = false) ReviewVerificationRequest request) {
        ReviewVerificationRequest body = request != null ? request : new ReviewVerificationRequest(null, null);
        return ApiResponse.of("Vérification refusée",
                verificationService.reject(id, SecurityUtils.currentUserId(), body));
    }

    @PostMapping("/verifications/{id}/require-more-info")
    public ApiResponse<VerificationResponse> requireMoreInfo(@PathVariable UUID id,
                                                                @RequestBody(required = false) ReviewVerificationRequest request) {
        ReviewVerificationRequest body = request != null ? request : new ReviewVerificationRequest(null, null);
        return ApiResponse.of("Complément d'information demandé",
                verificationService.requireMoreInformation(id, SecurityUtils.currentUserId(), body));
    }

    @GetMapping("/trips")
    public PagedResponse<TripResponse> allTrips(@RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Trajets récupérés", adminService.listAllTrips(PaginationUtils.of(page, size, null, null)));
    }

    @GetMapping("/bookings")
    public PagedResponse<BookingResponse> allBookings(@RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Réservations récupérées", adminService.listAllBookings(PaginationUtils.of(page, size, null, null)));
    }

    @GetMapping("/payments")
    public PagedResponse<PaymentResponse> allPayments(@RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Paiements récupérés", adminService.listAllPayments(PaginationUtils.of(page, size, null, null)));
    }

    @GetMapping("/reports")
    public PagedResponse<ReportResponse> allReports(@RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Signalements récupérés", reportService.listAll(PaginationUtils.of(page, size, null, null)));
    }

    @PostMapping("/reports/{id}/resolve")
    public ApiResponse<ReportResponse> resolveReport(@PathVariable UUID id, @RequestBody(required = false) ResolveReportRequest request) {
        String resolution = request != null ? request.resolution() : null;
        return ApiResponse.of("Signalement résolu",
                reportService.handle(id, SecurityUtils.currentUserId(), resolution, true));
    }

    @PostMapping("/reports/{id}/reject")
    public ApiResponse<ReportResponse> rejectReport(@PathVariable UUID id, @RequestBody(required = false) ResolveReportRequest request) {
        String resolution = request != null ? request.resolution() : null;
        return ApiResponse.of("Signalement rejeté",
                reportService.handle(id, SecurityUtils.currentUserId(), resolution, false));
    }

    /**
     * Modération d'un avis (masquer/republier). Route volontairement sous /admin/** — le rôle
     * MODERATOR n'a pas encore de route dédiée dans SecurityConfig (limitation connue, à
     * étendre : "/api/v1/admin/**".hasAnyRole("ADMIN","MODERATOR") le jour où ce rôle est utilisé).
     */
    @PostMapping("/reviews/{id}/moderate")
    public ApiResponse<Void> moderateReview(@PathVariable UUID id, @RequestParam boolean hide) {
        reviewService.moderate(id, SecurityUtils.currentUserId(), hide);
        return ApiResponse.ok(hide ? "Avis masqué" : "Avis republié");
    }

    /**
     * Résolution d'un litige (§51) — réservée à ADMIN, contrairement à sa consultation
     * (accessible aussi à CUSTOMER_SERVICE via GET /api/v1/customer-service/disputes).
     */
    @PostMapping("/disputes/{id}/resolve")
    public ApiResponse<DisputeResponse> resolveDispute(@PathVariable UUID id, @RequestBody(required = false) ResolveDisputeRequest request) {
        String resolution = request != null ? request.resolution() : null;
        return ApiResponse.of("Litige résolu",
                disputeService.resolve(id, SecurityUtils.currentUserId(), resolution, true));
    }

    @PostMapping("/disputes/{id}/reject")
    public ApiResponse<DisputeResponse> rejectDispute(@PathVariable UUID id, @RequestBody(required = false) ResolveDisputeRequest request) {
        String resolution = request != null ? request.resolution() : null;
        return ApiResponse.of("Litige rejeté",
                disputeService.resolve(id, SecurityUtils.currentUserId(), resolution, false));
    }
}
