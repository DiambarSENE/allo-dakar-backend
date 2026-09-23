package sn.diafoune.allo_dakar.mappers;

import org.springframework.stereotype.Component;
import sn.diafoune.allo_dakar.entities.Booking;
import sn.diafoune.allo_dakar.web.dtos.booking.BookingResponse;

@Component
public class BookingMapper {

    private final TripMapper tripMapper;

    public BookingMapper(TripMapper tripMapper) {
        this.tripMapper = tripMapper;
    }

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                tripMapper.toSummary(booking.getTrip()),
                booking.getPassenger().getId(),
                booking.getNumberOfSeats(),
                booking.getUnitPrice(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getPaymentStatus(),
                booking.getBookedAt(),
                booking.getConfirmedAt(),
                booking.getCancelledAt()
        );
    }
}
