package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.entities.DriverProfile;
import sn.diafoune.allo_dakar.entities.Vehicle;
import sn.diafoune.allo_dakar.entities.enums.VehicleStatus;
import sn.diafoune.allo_dakar.exceptions.BadRequestException;
import sn.diafoune.allo_dakar.exceptions.ForbiddenException;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.mappers.VehicleMapper;
import sn.diafoune.allo_dakar.repositories.VehicleRepository;
import sn.diafoune.allo_dakar.services.interfaces.DriverService;
import sn.diafoune.allo_dakar.services.interfaces.VehicleService;
import sn.diafoune.allo_dakar.web.dtos.vehicle.CreateVehicleRequest;
import sn.diafoune.allo_dakar.web.dtos.vehicle.UpdateVehicleRequest;
import sn.diafoune.allo_dakar.web.dtos.vehicle.VehicleResponse;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverService driverService;
    private final VehicleMapper vehicleMapper;

    @Override
    @Transactional
    public VehicleResponse create(UUID driverUserId, CreateVehicleRequest request) {
        if (vehicleRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new BadRequestException("Un véhicule avec cette immatriculation existe déjà");
        }
        DriverProfile driver = driverService.getOrCreateForUser(driverUserId);

        Vehicle vehicle = Vehicle.builder()
                .driver(driver)
                .brand(request.brand())
                .model(request.model())
                .registrationNumber(request.registrationNumber())
                .color(request.color())
                .year(request.year())
                .numberOfSeats(request.numberOfSeats())
                .vehicleType(request.vehicleType())
                .status(VehicleStatus.ACTIVE)
                .build();

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleResponse update(UUID driverUserId, UUID vehicleId, UpdateVehicleRequest request) {
        DriverProfile driver = driverService.getOrCreateForUser(driverUserId);
        Vehicle vehicle = getEntityOwnedBy(vehicleId, driver.getId());

        if (request.brand() != null) vehicle.setBrand(request.brand());
        if (request.model() != null) vehicle.setModel(request.model());
        if (request.color() != null) vehicle.setColor(request.color());
        if (request.year() != null) vehicle.setYear(request.year());
        if (request.numberOfSeats() != null) vehicle.setNumberOfSeats(request.numberOfSeats());
        if (request.vehicleType() != null) vehicle.setVehicleType(request.vehicleType());
        if (request.status() != null) vehicle.setStatus(request.status());

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public void delete(UUID driverUserId, UUID vehicleId) {
        DriverProfile driver = driverService.getOrCreateForUser(driverUserId);
        Vehicle vehicle = getEntityOwnedBy(vehicleId, driver.getId());
        vehicleRepository.delete(vehicle);
    }

    @Override
    public List<VehicleResponse> listMine(UUID driverUserId) {
        DriverProfile driver = driverService.getOrCreateForUser(driverUserId);
        return vehicleRepository.findByDriverId(driver.getId()).stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    @Override
    public Vehicle getEntityOwnedBy(UUID vehicleId, UUID driverProfileId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> ResourceNotFoundException.of("Vehicle", vehicleId));
        if (!vehicle.getDriver().getId().equals(driverProfileId)) {
            throw new ForbiddenException("Ce véhicule n'appartient pas à ce conducteur");
        }
        return vehicle;
    }
}
