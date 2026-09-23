package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.entities.AuditLog;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.UserStatus;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.mappers.UserMapper;
import sn.diafoune.allo_dakar.repositories.AuditLogRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;
import sn.diafoune.allo_dakar.security.keycloak.KeycloakAdminClient;
import sn.diafoune.allo_dakar.services.interfaces.UserService;
import sn.diafoune.allo_dakar.web.dtos.user.UpdateUserRequest;
import sn.diafoune.allo_dakar.web.dtos.user.UserResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditLogRepository auditLogRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    public User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    @Override
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(getEntityById(id));
    }

    @Override
    public org.springframework.data.domain.Page<UserResponse> search(String search, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<User> page = (search == null || search.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.search(search.trim(), pageable);
        return page.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse updateMe(UUID userId, UpdateUserRequest request) {
        User user = getEntityById(userId);
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.dateOfBirth() != null) user.setDateOfBirth(request.dateOfBirth());
        if (request.profilePicture() != null) user.setProfilePicture(request.profilePicture());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void suspend(UUID userId, UUID actingAdminId, String reason) {
        User user = getEntityById(userId);
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
        // Bloque immédiatement toute nouvelle authentification côté Keycloak — sans ce Sync,
        // un utilisateur suspendu localement pourrait quand même obtenir un token valide,
        // puisque l'authentification ne passe plus par ce backend (voir KeycloakAdminClient).
        keycloakAdminClient.setEnabled(user.getKeycloakId(), false);
        audit(actingAdminId, userId, "USER_SUSPENDED", reason);
    }

    @Override
    @Transactional
    public void activate(UUID userId, UUID actingAdminId) {
        User user = getEntityById(userId);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        keycloakAdminClient.setEnabled(user.getKeycloakId(), true);
        audit(actingAdminId, userId, "USER_ACTIVATED", null);
    }

    private void audit(UUID actorId, UUID targetUserId, String action, String details) {
        AuditLog log = AuditLog.builder()
                .actor(actorId != null ? getEntityById(actorId) : null)
                .entityType("User")
                .entityId(targetUserId)
                .action(action)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }
}
