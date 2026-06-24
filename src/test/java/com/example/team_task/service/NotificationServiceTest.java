package com.example.team_task.service;

import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.error.NotificationNotFoundException;
import com.example.team_task.dto.kafka.NotificationResponse;
import com.example.team_task.dto.kafka.UnreadCountResponse;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.Notification;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    private UserResponse currentUserResponse;
    private User testUser;
    private Task testTask;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        currentUserResponse = UserResponse.builder()
                .id(1L)
                .name("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");

        testTask = new Task();
        testTask.setId(10L);
        testTask.setTitle("Test Task");

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setNotificationId("notif-uuid-123");
        testNotification.setUser(testUser);
        testNotification.setType("TASK_ASSIGNED");
        testNotification.setTitle("New task assigned");
        testNotification.setMessage("You have been assigned a new task");
        testNotification.setTask(testTask);
        testNotification.setTriggeredBy(2L);
        testNotification.setRead(false);
        testNotification.setCreatedAt(LocalDateTime.of(2026, 6, 15, 10, 30));
    }

    @Test
    void getMyNotifications_UserHasNotifications_ReturnsNotificationList() {
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testNotification));

        List<NotificationResponse> responses = notificationService.getMyNotifications();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getNotificationId()).isEqualTo("notif-uuid-123");
        assertThat(responses.get(0).getType()).isEqualTo("TASK_ASSIGNED");
        assertThat(responses.get(0).getTitle()).isEqualTo("New task assigned");
        assertThat(responses.get(0).getMessage()).isEqualTo("You have been assigned a new task");
        assertThat(responses.get(0).getTaskId()).isEqualTo(10L);
        assertThat(responses.get(0).getTriggeredBy()).isEqualTo(2L);
        assertThat(responses.get(0).isRead()).isFalse();
    }

    @Test
    void getMyNotifications_UserHasNoNotifications_ReturnsEmptyList() {
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<NotificationResponse> responses = notificationService.getMyNotifications();

        assertThat(responses).isEmpty();
    }

    @Test
    void getMyNotifications_NotificationWithoutTask_ReturnsNullTaskId() {
        testNotification.setTask(null);

        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testNotification));

        List<NotificationResponse> responses = notificationService.getMyNotifications();

        assertThat(responses.get(0).getTaskId()).isNull();
    }

    @Test
    void getUnreadCount_UserHasUnreadNotifications_ReturnsCorrectCount() {
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);

        UnreadCountResponse response = notificationService.getUnreadCount();

        assertThat(response.getUnreadCount()).isEqualTo(5L);
    }

    @Test
    void getUnreadCount_UserHasNoUnreadNotifications_ReturnsZero() {
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(0L);

        UnreadCountResponse response = notificationService.getUnreadCount();

        assertThat(response.getUnreadCount()).isEqualTo(0L);
    }

    @Test
    void markAsRead_ExistingNotification_MarksAsRead() {
        when(notificationRepository.findByNotificationId("notif-uuid-123"))
                .thenReturn(Optional.of(testNotification));
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);

        notificationService.markAsRead("notif-uuid-123");

        assertThat(testNotification.isRead()).isTrue();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void markAsRead_NonExistentNotification_ThrowsNotificationNotFoundException() {
        when(notificationRepository.findByNotificationId("nonexistent"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead("nonexistent"))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void markAsRead_AlienNotification_ThrowsAccessDeniedException() {
        User otherUser = new User();
        otherUser.setId(99L);
        testNotification.setUser(otherUser);

        when(notificationRepository.findByNotificationId("notif-uuid-123"))
                .thenReturn(Optional.of(testNotification));
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);

        assertThatThrownBy(() -> notificationService.markAsRead("notif-uuid-123"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only manage your own notifications");

        verify(notificationRepository, never()).save(testNotification);
    }

    @Test
    void markAsRead_AdminAccess_AllowedToMarkAnyNotification() {
        User otherUser = new User();
        otherUser.setId(99L);
        testNotification.setUser(otherUser);

        UserResponse adminResponse = UserResponse.builder()
                .id(2L)
                .name("admin")
                .role("ADMIN")
                .build();

        when(notificationRepository.findByNotificationId("notif-uuid-123"))
                .thenReturn(Optional.of(testNotification));
        when(userService.getCurrentUser()).thenReturn(adminResponse);

        notificationService.markAsRead("notif-uuid-123");

        assertThat(testNotification.isRead()).isTrue();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void markAllAsRead_UserHasNotifications_MarksAllAsRead() {
        Notification secondNotification = new Notification();
        secondNotification.setId(2L);
        secondNotification.setNotificationId("notif-uuid-456");
        secondNotification.setUser(testUser);
        secondNotification.setRead(false);

        when(userService.getCurrentUser()).thenReturn(currentUserResponse);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testNotification, secondNotification));

        notificationService.markAllAsRead();

        assertThat(testNotification.isRead()).isTrue();
        assertThat(secondNotification.isRead()).isTrue();
    }

    @Test
    void deleteNotification_ExistingNotification_DeletesSuccessfully() {
        when(notificationRepository.findByNotificationId("notif-uuid-123"))
                .thenReturn(Optional.of(testNotification));
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);

        notificationService.deleteNotification("notif-uuid-123");

        verify(notificationRepository).delete(testNotification);
    }

    @Test
    void deleteNotification_NonExistentNotification_ThrowsNotificationNotFoundException() {
        when(notificationRepository.findByNotificationId("nonexistent"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.deleteNotification("nonexistent"))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("nonexistent");

        verify(notificationRepository, never()).delete(testNotification);
    }

    @Test
    void deleteNotification_AlienNotification_ThrowsAccessDeniedException() {
        User otherUser = new User();
        otherUser.setId(99L);
        testNotification.setUser(otherUser);

        when(notificationRepository.findByNotificationId("notif-uuid-123"))
                .thenReturn(Optional.of(testNotification));
        when(userService.getCurrentUser()).thenReturn(currentUserResponse);

        assertThatThrownBy(() -> notificationService.deleteNotification("notif-uuid-123"))
                .isInstanceOf(AccessDeniedException.class);

        verify(notificationRepository, never()).delete(testNotification);
    }
}