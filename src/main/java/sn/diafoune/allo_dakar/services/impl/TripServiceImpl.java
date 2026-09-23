package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.commons.configs.BusinessRuleProperties;
import sn.diafoune.allo_dakar.commons.utils.DateUtils;
import sn.diafoune.allo_dakar.entities.DriverProfile;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.Vehicle;
import sn.diafoune.allo_dakar.entities.enums.DriverVerificationStatus;
import sn.diafoune.allo_dakar.entities.enums.TripStatus;
import sn.diafoune.allo_dakar.exceptions.BusinessException;
import sn.diafoune.allo_dakar.exceptions.ErrorCode;
import sn.diafoune.allo_dakar.exceptions.ForbiddenException;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.mappers.TripMapper;
import sn.diafoune.allo_dakar.repositories.TripRepository;
import sn.diafoune.allo_dakar.repositories.TripSpecifications;
import sn.diafoune.allo_dakar.services.interfaces.DriverService;
import sn.diafoune.allo_dakar.services.interfaces.TripService;
import sn.diafoune.allo_dakar.services.interfaces.VehicleService;
import sn.diafoune.allo_dakar.web.dtos.trip.CreateTripRequest;
import sn.diafoune.allo_dakar.web.dtos.trip.TripResponse;
import sn.diafoune.allo_dakar.web.dtos.trip.TripSearchRequest;
import sn.diafoune.allo_dakar.web.dtos.trip.UpdateTripRequest;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private static final Logger log = LoggerFactory.getLogger(TripServiceImpl.class);

    private final TripRepository tripRepository;
    private final DriverService driverService;
    private final VehicleService vehicleService;
    private final TripMapper tripMapper;
    private final BusinessRuleProperties businessRuleProperties;

    @Override
    @Transactional
    public TripResponse create(UUID driverUserId, CreateTripRequest request) {
        DriverProfile driver = driverService.getOrCreateForUser(driverUserId);
        Vehicle vehicle = vehicleService.getEntityOwnedBy(request.vehicleId(), driver.getId());

        if (request.totalSeats() > vehicle.getNumberOfSeats()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Le nombre de places proposées dépasse la capacité du véhicule");
        }

        Trip trip = Trip.builder()
                .driver(driver)
                .vehicle(vehicle)
                .departureCity(request.departureCity())
                .departureAddress(request.departureAddress())
                .departureLatitude(request.departureLatitude())
                .departureLongitude(request.departureLongitude())
                .destinationCity(request.destinationCity())
                .destinationAddress(request.destinationAddress())
                .destinationLatitude(request.destinationLatitude())
                .destinationLongitude(request.destinationLongitude())
                .departureDate(request.departureDate())
                .departureTime(request.departureTime())
                .meetingPoint(request.meetingPoint())
                .pricePerSeat(request.pricePerSeat())
                .totalSeats(request.totalSeats())
                .availableSeats(request.totalSeats())
                .description(request.description())
                .rules(request.rules())
                .status(TripStatus.DRAFT)
                .build();

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    @Transactional
    public TripResponse update(UUID driverUserId, UUID tripId, UpdateTripRequest request) {
        Trip trip = getOwnedEntity(tripId, driverUserId);
        if (trip.getStatus() != TripStatus.DRAFT && trip.getStatus() != TripStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.TRIP_NOT_PUBLISHABLE,
                    "Ce trajet ne peut plus être modifié dans son état actuel");
        }

        if (request.departureAddress() != null) trip.setDepartureAddress(request.departureAddress());
        if (request.destinationAddress() != null) trip.setDestinationAddress(request.destinationAddress());
        if (request.departureDate() != null) trip.setDepartureDate(request.departureDate());
        if (request.departureTime() != null) trip.setDepartureTime(request.departureTime());
        if (request.meetingPoint() != null) trip.setMeetingPoint(request.meetingPoint());
        if (request.pricePerSeat() != null) trip.setPricePerSeat(request.pricePerSeat());
        if (request.description() != null) trip.setDescription(request.description());
        if (request.rules() != null) trip.setRules(request.rules());

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getById(UUID tripId) {
        return tripMapper.toResponse(getEntityById(tripId));
    }

    @Override
    public Trip getEntityById(UUID tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> ResourceNotFoundException.of("Trip", tripId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> search(TripSearchRequest criteria, Pageable pageable) {
        var spec = TripSpecifications.search(
                criteria.departureCity(), criteria.destinationCity(), criteria.departureDate(),
                criteria.minPrice(), criteria.maxPrice(), criteria.minAvailableSeats(), criteria.driver());
        return tripRepository.findAll(spec, pageable).map(tripMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> listMine(UUID driverUserId, Pageable pageable) {
        DriverProfile driver = driverService.getOrCreateForUser(driverUserId);
        return tripRepository.findByDriverId(driver.getId(), pageable).map(tripMapper::toResponse);
    }

    @Override
    @Transactional
    public TripResponse publish(UUID driverUserId, UUID tripId) {
        Trip trip = getOwnedEntity(tripId, driverUserId);
        DriverProfile driver = trip.getDriver();

        // Règle §48-1, centralisée et configurable (app.business.driver-verification-required-to-publish).
        if (businessRuleProperties.driverVerificationRequiredToPublish()
                && driver.getVerificationStatus() != DriverVerificationStatus.VERIFIED) {
            throw new BusinessException(ErrorCode.DRIVER_NOT_VERIFIED,
                    "Le conducteur doit être vérifié avant de publier un trajet");
        }
        if (DateUtils.isPast(trip.getDepartureDate(), trip.getDepartureTime())) {
            throw new BusinessException(ErrorCode.TRIP_NOT_PUBLISHABLE, "La date de départ est déjà passée");
        }
        if (trip.getStatus() != TripStatus.DRAFT) {
            throw new BusinessException(ErrorCode.TRIP_NOT_PUBLISHABLE,
                    "Seul un trajet en brouillon peut être publié");
        }

        trip.setStatus(TripStatus.PUBLISHED);
        trip.setPublishedAt(Instant.now());
        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    @Transactional
    public TripResponse cancel(UUID driverUserId, UUID tripId, String reason) {
        Trip trip = getOwnedEntity(tripId, driverUserId);
        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.TRIP_NOT_PUBLISHABLE,
                    "Ce trajet ne peut plus être annulé dans son état actuel");
        }
        trip.setStatus(TripStatus.CANCELLED);
        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 */15 * * * *")
    public int expireOverdueTrips() {
        List<Trip> candidates = tripRepository.findByStatusIn(List.of(TripStatus.PUBLISHED, TripStatus.FULL));
        int expired = 0;
        for (Trip trip : candidates) {
            if (DateUtils.isPast(trip.getDepartureDate(), trip.getDepartureTime())) {
                trip.setStatus(TripStatus.EXPIRED);
                tripRepository.save(trip);
                expired++;
            }
        }
        if (expired > 0) {
            log.info("TRIP_EXPIRATION_JOB expiredCount={}", expired);
        }
        return expired;
    }

    private Trip getOwnedEntity(UUID tripId, UUID driverUserId) {
        Trip trip = getEntityById(tripId);
        DriverProfile driver = driverService.getOrCreateForUser(driverUserId);
        if (!trip.getDriver().getId().equals(driver.getId())) {
            throw new ForbiddenException("Ce trajet n'appartient pas à ce conducteur");
        }
        return trip;
    }
}
