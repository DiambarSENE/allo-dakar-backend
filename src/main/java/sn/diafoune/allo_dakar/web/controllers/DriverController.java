package sn.diafoune.allo_dakar.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import sn.diafoune.allo_dakar.services.interfaces.DriverService;
import sn.diafoune.allo_dakar.services.interfaces.TripService;
import sn.diafoune.allo_dakar.services.interfaces.VehicleService;
import sn.diafoune.allo_dakar.services.interfaces.VerificationService;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.common.PagedResponse;
import sn.diafoune.allo_dakar.web.dtos.driver.DriverProfileResponse;
import sn.diafoune.allo_dakar.web.dtos.driver.UpdateDriverProfileRequest;
import sn.diafoune.allo_dakar.web.dtos.trip.TripResponse;
import sn.diafoune.allo_dakar.web.dtos.vehicle.CreateVehicleRequest;
import sn.diafoune.allo_dakar.web.dtos.vehicle.UpdateVehicleRequest;
import sn.diafoune.allo_dakar.web.dtos.vehicle.VehicleResponse;
import sn.diafoune.allo_dakar.web.dtos.verification.SubmitVerificationRequest;
import sn.diafoune.allo_dakar.web.dtos.verification.VerificationResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final VehicleService vehicleService;
    private final VerificationService verificationService;
    private final TripService tripService;

    @GetMapping("/{id}")
    public ApiResponse<DriverProfileResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of("Profil conducteur récupéré", driverService.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public ApiResponse<DriverProfileResponse> getMyProfile() {
        return ApiResponse.of("Profil conducteur récupéré",
                driverService.getMyProfile(SecurityUtils.currentUserId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public ApiResponse<DriverProfileResponse> updateMe(@Valid @RequestBody UpdateDriverProfileRequest request) {
        return ApiResponse.of("Profil conducteur mis à jour",
                driverService.updateMe(SecurityUtils.currentUserId(), request));
    }

    @PostMapping("/me/verification")
    @PreAuthorize("hasRole('DRIVER')")
    public ApiResponse<VerificationResponse> submitVerification(@Valid @RequestBody SubmitVerificationRequest request) {
        return ApiResponse.of("Document de vérification soumis",
                verificationService.submit(SecurityUtils.currentUserId(), request));
    }

    @GetMapping("/me/vehicles")
    @PreAuthorize("hasRole('DRIVER')")
    public ApiResponse<List<VehicleResponse>> myVehicles() {
        return ApiResponse.of("Véhicules récupérés", vehicleService.listMine(SecurityUtils.currentUserId()));
    }

    @PostMapping("/me/vehicles")
    @PreAuthorize("hasRole('DRIVER')")
    public ApiResponse<VehicleResponse> addVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        return ApiResponse.of("Véhicule ajouté", vehicleService.create(SecurityUtils.currentUserId(), request));
    }

    @PutMapping("/me/vehicles/{vehicleId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ApiResponse<VehicleResponse> updateVehicle(@PathVariable UUID vehicleId,
                                                        @Valid @RequestBody UpdateVehicleRequest request) {
        return ApiResponse.of("Véhicule mis à jour",
                vehicleService.update(SecurityUtils.currentUserId(), vehicleId, request));
    }

    @DeleteMapping("/me/vehicles/{vehicleId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ApiResponse<Void> deleteVehicle(@PathVariable UUID vehicleId) {
        vehicleService.delete(SecurityUtils.currentUserId(), vehicleId);
        return ApiResponse.ok("Véhicule supprimé");
    }

    @GetMapping("/me/trips")
    @PreAuthorize("hasRole('DRIVER')")
    public PagedResponse<TripResponse> myTrips(@RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Trajets récupérés",
                tripService.listMine(SecurityUtils.currentUserId(), PaginationUtils.of(page, size, null, null)));
    }
}
