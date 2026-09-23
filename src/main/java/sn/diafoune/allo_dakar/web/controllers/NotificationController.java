package sn.diafoune.allo_dakar.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.diafoune.allo_dakar.commons.utils.PaginationUtils;
import sn.diafoune.allo_dakar.commons.utils.SecurityUtils;
import sn.diafoune.allo_dakar.mappers.NotificationMapper;
import sn.diafoune.allo_dakar.services.interfaces.NotificationService;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.common.PagedResponse;
import sn.diafoune.allo_dakar.web.dtos.notification.NotificationResponse;

import java.util.UUID;

/** Seul le canal IN_APP est consultable via l'API (voir NotificationServiceImpl). */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @GetMapping("/me")
    public PagedResponse<NotificationResponse> mine(@RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Notifications récupérées",
                notificationService.listMine(SecurityUtils.currentUserId(), PaginationUtils.of(page, size, null, null))
                        .map(notificationMapper::toResponse));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable UUID id) {
        notificationService.markRead(id, SecurityUtils.currentUserId());
        return ApiResponse.ok("Notification marquée comme lue");
    }
}
