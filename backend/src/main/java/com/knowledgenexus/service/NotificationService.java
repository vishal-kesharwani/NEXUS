package com.knowledgenexus.service;

import com.knowledgenexus.dto.NotificationResponse;
import com.knowledgenexus.model.Notification;
import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;

    public List<NotificationResponse> getNotifications(String email) {
        User user = currentUserService.resolve(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    public long getUnreadCount(String email) {
        User user = currentUserService.resolve(email);
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
        
        pushNotificationUpdate(notification.getUser());
        
        return map(notification);
    }

    public NotificationResponse createNotification(User user, String title, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        
        pushNotificationUpdate(user);
        
        return map(saved);
    }

    private void pushNotificationUpdate(User user) {
        long unreadCount = notificationRepository.countByUserIdAndReadFalse(user.getId());
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + user.getId(),
                new NotificationUpdate(unreadCount)
        );
    }

    private NotificationResponse map(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public static record NotificationUpdate(long unreadCount) {}
}
