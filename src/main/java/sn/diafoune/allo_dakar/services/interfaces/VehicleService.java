package sn.diafoune.allo_dakar.services.interfaces;

import sn.diafoune.allo_dakar.entities.Vehicle;
import sn.diafoune.allo_dakar.web.dtos.vehicle.CreateVehicleRequest;
import sn.diafoune.allo_dakar.web.dtos.vehicle.UpdateVehicleRequest;
import sn.diafoune.allo_dakar.web.dtos.vehicle.VehicleResponse;

import java.util.List;
import java.util.UUID;

public interface VehicleService {

    VehicleResponse create(UUID driverUserId, CreateVehicleRequest request);

    VehicleResponse update(UUID driverUserId, UUID vehicleId, UpdateVehicleRequest request);

    void delete(UUID driverUserId, UUID vehicleId);

    List<VehicleResponse> listMine(UUID driverUserId);

    Vehicle getEntityOwnedBy(UUID vehicleId, UUID driverProfileId);
}
