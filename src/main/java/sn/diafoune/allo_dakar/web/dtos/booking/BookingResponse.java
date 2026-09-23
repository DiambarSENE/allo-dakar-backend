package sn.diafoune.allo_dakar.web.dtos.booking;

import sn.diafoune.allo_dakar.entities.enums.BookingStatus;
import sn.diafoune.allo_dakar.entities.enums.PaymentStatus;
import sn.diafoune.allo_dakar.web.dtos.trip.TripSummaryResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        String bookingReference,
        TripSummaryResponse trip,
        UUID passengerId,
        int numberOfSeats,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        BookingStatus status,
        PaymentStatus paymentStatus,
        Instant bookedAt,
        Instant confirmedAt,
        Instant cancelledAt
) {
}
