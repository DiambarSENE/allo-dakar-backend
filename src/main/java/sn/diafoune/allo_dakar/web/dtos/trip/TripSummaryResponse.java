package sn.diafoune.allo_dakar.web.dtos.trip;

import sn.diafoune.allo_dakar.entities.enums.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** Version allégée pour les listes/recherches — évite de charger le véhicule complet. */
public record TripSummaryResponse(
        UUID id,
        String departureCity,
        String destinationCity,
        LocalDate departureDate,
        LocalTime departureTime,
        BigDecimal pricePerSeat,
        int availableSeats,
        TripStatus status
) {
}
