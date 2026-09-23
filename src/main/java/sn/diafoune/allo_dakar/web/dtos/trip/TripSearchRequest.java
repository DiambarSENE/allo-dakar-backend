package sn.diafoune.allo_dakar.web.dtos.trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TripSearchRequest(
        String departureCity,
        String destinationCity,
        LocalDate departureDate,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minAvailableSeats,
        UUID driver
) {
}
