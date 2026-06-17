package com.knowledgenexus.controller;

import com.knowledgenexus.dto.NotificationResponse;
import com.knowledgenexus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return notificationService.getNotifications(userDetails.getUsername());
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return notificationService.getUnreadCount(userDetails.getUsername());
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable UUID id
    ) {
        return notificationService.markAsRead(id);
    }
}
