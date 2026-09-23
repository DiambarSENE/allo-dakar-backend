package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.entities.DriverProfile;
import sn.diafoune.allo_dakar.entities.Role;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.RoleType;
import sn.diafoune.allo_dakar.exceptions.BadRequestException;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.mappers.DriverMapper;
import sn.diafoune.allo_dakar.repositories.DriverProfileRepository;
import sn.diafoune.allo_dakar.repositories.ReviewRepository;
import sn.diafoune.allo_dakar.repositories.RoleRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;
import sn.diafoune.allo_dakar.services.interfaces.DriverService;
import sn.diafoune.allo_dakar.web.dtos.driver.DriverProfileResponse;
import sn.diafoune.allo_dakar.web.dtos.driver.UpdateDriverProfileRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ReviewRepository reviewRepository;
    private final DriverMapper driverMapper;

    @Override
    @Transactional
    public DriverProfile getOrCreateForUser(UUID userId) {
        return driverProfileRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

            Role driverRole = roleRepository.findByName(RoleType.DRIVER)
                    .orElseThrow(() -> new IllegalStateException("Rôle DRIVER manquant en base"));
            if (user.getRoles().stream().noneMatch(r -> r.getName() == RoleType.DRIVER)) {
                HashSet<Role> roles = new HashSet<>(user.getRoles());
                roles.add(driverRole);
                user.setRoles(roles);
                userRepository.save(user);
            }

            DriverProfile profile = DriverProfile.builder()
                    .user(user)
                    .licenseNumber("PENDING-" + user.getId().toString().substring(0, 8).toUpperCase())
                    .licenseExpiryDate(LocalDate.now().plusYears(1))
                    .build();
            return driverProfileRepository.save(profile);
        });
    }

    @Override
    public DriverProfile getEntityById(UUID driverProfileId) {
        return driverProfileRepository.findById(driverProfileId)
                .orElseThrow(() -> ResourceNotFoundException.of("DriverProfile", driverProfileId));
    }

    @Override
    public DriverProfileResponse getById(UUID driverProfileId) {
        return driverMapper.toResponse(getEntityById(driverProfileId));
    }

    @Override
    @Transactional
    public DriverProfileResponse getMyProfile(UUID userId) {
        return driverMapper.toResponse(getOrCreateForUser(userId));
    }

    @Override
    @Transactional
    public DriverProfileResponse updateMe(UUID userId, UpdateDriverProfileRequest request) {
        DriverProfile profile = getOrCreateForUser(userId);
        if (request.licenseNumber() != null) {
            if (driverProfileRepository.existsByLicenseNumber(request.licenseNumber())
                    && !request.licenseNumber().equals(profile.getLicenseNumber())) {
                throw new BadRequestException("Ce numéro de permis est déjà utilisé");
            }
            profile.setLicenseNumber(request.licenseNumber());
        }
        if (request.licenseExpiryDate() != null) {
            profile.setLicenseExpiryDate(request.licenseExpiryDate());
        }
        return driverMapper.toResponse(driverProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public void recalculateAfterCompletedTrip(UUID driverProfileId) {
        DriverProfile profile = getEntityById(driverProfileId);
        profile.setTotalTrips(profile.getTotalTrips() + 1);
        Double avg = reviewRepository.computeAverageRating(profile.getUser().getId());
        if (avg != null) {
            profile.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        }
        driverProfileRepository.save(profile);
    }
}
