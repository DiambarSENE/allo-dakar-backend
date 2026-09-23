package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.PassengerProfile;

import java.util.Optional;
import java.util.UUID;

public interface PassengerProfileRepository extends JpaRepository<PassengerProfile, UUID> {

    Optional<PassengerProfile> findByUserId(UUID userId);
}
