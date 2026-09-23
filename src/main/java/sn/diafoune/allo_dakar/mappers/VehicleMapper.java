package sn.diafoune.allo_dakar.mappers;

import org.springframework.stereotype.Component;
import sn.diafoune.allo_dakar.entities.Vehicle;
import sn.diafoune.allo_dakar.web.dtos.vehicle.VehicleResponse;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getDriver().getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getRegistrationNumber(),
                vehicle.getColor(),
                vehicle.getYear(),
                vehicle.getNumberOfSeats(),
                vehicle.getVehicleType(),
                vehicle.getStatus()
        );
    }
}
