package com.example.team_task.service;


import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.error.NotificationNotFoundException;
import com.example.team_task.dto.kafka.NotificationResponse;
import com.example.team_task.dto.kafka.UnreadCountResponse;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Notification;
import com.example.team_task.entity.User;
import com.example.team_task.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public List<NotificationResponse> getMyNotifications() {
        UserResponse currentUser = userService.getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UnreadCountResponse getUnreadCount() {
        UserResponse currentUser = userService.getCurrentUser();
        long count = notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId());
        return UnreadCountResponse.builder().unreadCount(count).build();
    }

    @Transactional
    public void markAsRead(String notificationId) {
        Notification notification = findByNotificationId(notificationId);
        checkOwnership(notification);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        UserResponse currentUser = userService.getCurrentUser();
        notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .forEach(n -> n.setRead(true));
    }

    @Transactional
    public void deleteNotification(String notificationId) {
        Notification notification = findByNotificationId(notificationId);
        checkOwnership(notification);
        notificationRepository.delete(notification);
    }

    private Notification findByNotificationId(String notificationId) {
        return notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    }

    private void checkOwnership(Notification notification) {
        UserResponse currentUser = userService.getCurrentUser();
        if (!notification.getUser().getId().equals(currentUser.getId())
                && !currentUser.getRole().equals("ADMIN")) {
            throw new AccessDeniedException("You can only manage your own notifications");
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .taskId(notification.getTask() != null ? notification.getTask().getId() : null)
                .triggeredBy(notification.getTriggeredBy())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
