package com.example.team_task.kafka;

import com.example.team_task.dto.kafka.NotificationEvent;
import com.example.team_task.entity.Notification;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.repository.NotificationRepository;
import com.example.team_task.repository.TaskRepository;
import com.example.team_task.repository.UserRepository;
import com.example.team_task.service.NotificationConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    private NotificationEvent testEvent;
    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");

        testTask = new Task();
        testTask.setId(10L);
        testTask.setTitle("Test Task");

        testEvent = NotificationEvent.builder()
                .notificationId("notif-uuid-456")
                .type("TASK_ASSIGNED")
                .userId(1L)
                .title("New task")
                .message("You have a new task")
                .taskId(10L)
                .triggeredBy(2L)
                .createdAt(LocalDateTime.of(2026, 6, 15, 10, 30))
                .build();
    }

    @Test
    void consume_ValidEventWithTask_SavesNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(testTask));

        notificationConsumer.consume(testEvent);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getNotificationId()).isEqualTo("notif-uuid-456");
        assertThat(saved.getType()).isEqualTo("TASK_ASSIGNED");
        assertThat(saved.getTitle()).isEqualTo("New task");
        assertThat(saved.getMessage()).isEqualTo("You have a new task");
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getTask()).isEqualTo(testTask);
        assertThat(saved.getTriggeredBy()).isEqualTo(2L);
        assertThat(saved.isRead()).isFalse();
        assertThat(saved.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 15, 10, 30));
    }

    @Test
    void consume_EventWithoutTask_SavesNotificationWithNullTask() {
        testEvent.setTaskId(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        notificationConsumer.consume(testEvent);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getTask()).isNull();
    }

    @Test
    void consume_UserNotFound_DoesNotSaveNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        notificationConsumer.consume(testEvent);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void consume_TaskNotFound_SavesNotificationWithNullTask() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(10L)).thenReturn(Optional.empty());

        notificationConsumer.consume(testEvent);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getTask()).isNull();
    }

    @Test
    void consume_NotificationWithoutTriggeredBy_SavesCorrectly() {
        testEvent.setTriggeredBy(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(testTask));

        notificationConsumer.consume(testEvent);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getTriggeredBy()).isNull();
    }
}