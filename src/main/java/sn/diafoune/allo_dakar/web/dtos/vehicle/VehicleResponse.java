package sn.diafoune.allo_dakar.web.dtos.vehicle;

import sn.diafoune.allo_dakar.entities.enums.VehicleStatus;
import sn.diafoune.allo_dakar.entities.enums.VehicleType;

import java.util.UUID;

public record VehicleResponse(
        UUID id,
        UUID driverId,
        String brand,
        String model,
        String registrationNumber,
        String color,
        Integer year,
        int numberOfSeats,
        VehicleType vehicleType,
        VehicleStatus status
) {
}
