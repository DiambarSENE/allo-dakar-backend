package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    List<Vehicle> findByDriverId(UUID driverId);

    Optional<Vehicle> findByIdAndDriverId(UUID id, UUID driverId);

    boolean existsByRegistrationNumber(String registrationNumber);
}
