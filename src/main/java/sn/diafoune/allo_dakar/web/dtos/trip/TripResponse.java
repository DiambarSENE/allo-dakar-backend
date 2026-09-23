package sn.diafoune.allo_dakar.web.dtos.trip;

import sn.diafoune.allo_dakar.entities.enums.TripStatus;
import sn.diafoune.allo_dakar.web.dtos.vehicle.VehicleResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TripResponse(
        UUID id,
        /** ID du compte utilisateur (User) du conducteur — PAS celui de son DriverProfile interne. */
        UUID driverId,
        String driverFullName,
        VehicleResponse vehicle,
        String departureCity,
        String departureAddress,
        String destinationCity,
        String destinationAddress,
        LocalDate departureDate,
        LocalTime departureTime,
        String meetingPoint,
        BigDecimal pricePerSeat,
        int availableSeats,
        int totalSeats,
        String description,
        String rules,
        TripStatus status,
        Instant publishedAt
) {
}
