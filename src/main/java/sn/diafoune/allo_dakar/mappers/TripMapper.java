package sn.diafoune.allo_dakar.mappers;

import org.springframework.stereotype.Component;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.web.dtos.trip.TripResponse;
import sn.diafoune.allo_dakar.web.dtos.trip.TripSummaryResponse;

@Component
public class TripMapper {

    private final VehicleMapper vehicleMapper;

    public TripMapper(VehicleMapper vehicleMapper) {
        this.vehicleMapper = vehicleMapper;
    }

    public TripResponse toResponse(Trip trip) {
        if (trip == null) {
            return null;
        }
        String driverFullName = trip.getDriver().getUser().getFirstName() + " " + trip.getDriver().getUser().getLastName();
        return new TripResponse(
                trip.getId(),
                trip.getDriver().getUser().getId(),
                driverFullName,
                vehicleMapper.toResponse(trip.getVehicle()),
                trip.getDepartureCity(),
                trip.getDepartureAddress(),
                trip.getDestinationCity(),
                trip.getDestinationAddress(),
                trip.getDepartureDate(),
                trip.getDepartureTime(),
                trip.getMeetingPoint(),
                trip.getPricePerSeat(),
                trip.getAvailableSeats(),
                trip.getTotalSeats(),
                trip.getDescription(),
                trip.getRules(),
                trip.getStatus(),
                trip.getPublishedAt()
        );
    }

    public TripSummaryResponse toSummary(Trip trip) {
        if (trip == null) {
            return null;
        }
        return new TripSummaryResponse(
                trip.getId(),
                trip.getDepartureCity(),
                trip.getDestinationCity(),
                trip.getDepartureDate(),
                trip.getDepartureTime(),
                trip.getPricePerSeat(),
                trip.getAvailableSeats(),
                trip.getStatus()
        );
    }
}
