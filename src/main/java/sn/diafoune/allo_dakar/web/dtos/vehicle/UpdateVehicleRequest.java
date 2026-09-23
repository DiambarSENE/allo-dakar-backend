package sn.diafoune.allo_dakar.web.dtos.vehicle;

import sn.diafoune.allo_dakar.entities.enums.VehicleStatus;
import sn.diafoune.allo_dakar.entities.enums.VehicleType;

public record UpdateVehicleRequest(
        String brand,
        String model,
        String color,
        Integer year,
        Integer numberOfSeats,
        VehicleType vehicleType,
        VehicleStatus status
) {
}
