package sn.diafoune.allo_dakar.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.diafoune.allo_dakar.commons.utils.SecurityUtils;
import sn.diafoune.allo_dakar.services.interfaces.UserService;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.user.UpdateUserRequest;
import sn.diafoune.allo_dakar.web.dtos.user.UserResponse;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.of("Profil récupéré", userService.getById(SecurityUtils.currentUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.of("Profil mis à jour", userService.updateMe(SecurityUtils.currentUserId(), request));
    }

    @PatchMapping("/me")
    public ApiResponse<UserResponse> patchMe(@Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.of("Profil mis à jour", userService.updateMe(SecurityUtils.currentUserId(), request));
    }
}
