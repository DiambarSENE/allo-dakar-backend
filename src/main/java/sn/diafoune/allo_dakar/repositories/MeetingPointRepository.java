package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.MeetingPoint;

import java.util.List;
import java.util.UUID;

public interface MeetingPointRepository extends JpaRepository<MeetingPoint, UUID> {

    List<MeetingPoint> findByTripId(UUID tripId);
}
