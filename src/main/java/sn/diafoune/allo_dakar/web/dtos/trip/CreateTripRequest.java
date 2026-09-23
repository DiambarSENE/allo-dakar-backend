package sn.diafoune.allo_dakar.web.dtos.trip;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateTripRequest(
        @NotNull UUID vehicleId,
        @NotBlank String departureCity,
        String departureAddress,
        Double departureLatitude,
        Double departureLongitude,
        @NotBlank String destinationCity,
        String destinationAddress,
        Double destinationLatitude,
        Double destinationLongitude,
        @NotNull @Future LocalDate departureDate,
        @NotNull LocalTime departureTime,
        String meetingPoint,
        @NotNull @PositiveOrZero BigDecimal pricePerSeat,
        @NotNull @Positive Integer totalSeats,
        String description,
        String rules
) {
}
