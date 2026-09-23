package sn.diafoune.allo_dakar.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.web.dtos.trip.CreateTripRequest;
import sn.diafoune.allo_dakar.web.dtos.trip.TripResponse;
import sn.diafoune.allo_dakar.web.dtos.trip.TripSearchRequest;
import sn.diafoune.allo_dakar.web.dtos.trip.UpdateTripRequest;

import java.util.UUID;

public interface TripService {

    TripResponse create(UUID driverUserId, CreateTripRequest request);

    TripResponse update(UUID driverUserId, UUID tripId, UpdateTripRequest request);

    TripResponse getById(UUID tripId);

    Trip getEntityById(UUID tripId);

    Page<TripResponse> search(TripSearchRequest criteria, Pageable pageable);

    Page<TripResponse> listMine(UUID driverUserId, Pageable pageable);

    /** Règle §48-1 : nécessite User.status=ACTIVE et (selon config) DriverProfile vérifié. */
    TripResponse publish(UUID driverUserId, UUID tripId);

    TripResponse cancel(UUID driverUserId, UUID tripId, String reason);

    /** Appelé par le job planifié (ambiguïté Phase 1 #3) pour faire passer PUBLISHED → EXPIRED. */
    int expireOverdueTrips();
}
