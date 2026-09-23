package sn.diafoune.allo_dakar.web.dtos.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID tripId,
        @NotNull @Positive Integer numberOfSeats
) {
}
