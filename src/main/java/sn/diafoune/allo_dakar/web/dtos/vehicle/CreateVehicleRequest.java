package sn.diafoune.allo_dakar.web.dtos.vehicle;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import sn.diafoune.allo_dakar.entities.enums.VehicleType;

public record CreateVehicleRequest(
        @NotBlank String brand,
        @NotBlank String model,
        @NotBlank String registrationNumber,
        String color,
        @Min(1990) Integer year,
        @Positive int numberOfSeats,
        @NotNull VehicleType vehicleType
) {
}
