package sn.diafoune.allo_dakar.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.enums.TripStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * La recherche paginée/filtrée passe par JpaSpecificationExecutor (voir TripSpecifications).
 *
 * findByIdForUpdate est une alternative PESSIMISTIC_WRITE, non utilisée par défaut : la
 * stratégie retenue pour la réservation est le verrouillage optimiste (@Version sur Trip),
 * avec retry applicatif borné côté BookingServiceImpl. Ce verrou pessimiste reste disponible si
 * le taux de conflits de version s'avère trop élevé en charge réelle (voir analyse Phase 1 §6).
 */
public interface TripRepository extends JpaRepository<Trip, UUID>, JpaSpecificationExecutor<Trip> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trip t where t.id = :id")
    Optional<Trip> findByIdForUpdate(@Param("id") UUID id);

    /** Utilisé par le job d'expiration (TripServiceImpl#expireOverdueTrips) — voir Phase 1, ambiguïté #3. */
    List<Trip> findByStatusIn(Collection<TripStatus> statuses);

    /** "Mes trajets" côté conducteur : tous les statuts, contrairement à la recherche publique. */
    Page<Trip> findByDriverId(UUID driverId, Pageable pageable);
}
