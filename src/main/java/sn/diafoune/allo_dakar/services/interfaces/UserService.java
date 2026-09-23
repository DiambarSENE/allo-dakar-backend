package sn.diafoune.allo_dakar.services.interfaces;

import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.web.dtos.user.UpdateUserRequest;
import sn.diafoune.allo_dakar.web.dtos.user.UserResponse;

import java.util.UUID;

public interface UserService {

    User getEntityById(UUID id);

    UserResponse getById(UUID id);

    UserResponse updateMe(UUID userId, UpdateUserRequest request);

    /** §43 "Rechercher un utilisateur" (service client/admin) — recherche vide = liste complète paginée. */
    org.springframework.data.domain.Page<UserResponse> search(String search, org.springframework.data.domain.Pageable pageable);

    void suspend(UUID userId, UUID actingAdminId, String reason);

    void activate(UUID userId, UUID actingAdminId);
}
