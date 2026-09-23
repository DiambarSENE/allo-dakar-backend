package sn.diafoune.allo_dakar.web.dtos.trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Modification autorisée uniquement tant que le trajet est DRAFT ou PUBLISHED sans réservation confirmée. */
public record UpdateTripRequest(
        String departureAddress,
        String destinationAddress,
        LocalDate departureDate,
        LocalTime departureTime,
        String meetingPoint,
        BigDecimal pricePerSeat,
        String description,
        String rules
) {
}
