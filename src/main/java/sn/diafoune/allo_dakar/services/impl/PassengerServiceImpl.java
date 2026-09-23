package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.entities.PassengerProfile;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.repositories.PassengerProfileRepository;
import sn.diafoune.allo_dakar.repositories.ReviewRepository;
import sn.diafoune.allo_dakar.services.interfaces.PassengerService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerProfileRepository passengerProfileRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public PassengerProfile getOrCreateForUser(User user) {
        return passengerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> passengerProfileRepository.save(
                        PassengerProfile.builder().user(user).build()));
    }

    @Override
    @Transactional
    public void recalculateAfterCompletedTrip(UUID passengerUserId) {
        passengerProfileRepository.findByUserId(passengerUserId).ifPresent(profile -> {
            profile.setTotalTrips(profile.getTotalTrips() + 1);
            Double avg = reviewRepository.computeAverageRating(passengerUserId);
            if (avg != null) {
                profile.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            }
            passengerProfileRepository.save(profile);
        });
    }
}
