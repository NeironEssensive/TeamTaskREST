package com.example.team_task.service;

import com.example.team_task.dto.kafka.CommentEvent;
import com.example.team_task.dto.kafka.NotificationEvent;
import com.example.team_task.dto.kafka.TaskEvent;
import com.example.team_task.dto.kafka.UserEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    void sendTaskEvent_ValidEvent_SendsToCorrectTopic() {
        TaskEvent event = TaskEvent.builder()
                .eventId("task-event-1")
                .eventType("TASK_CREATED")
                .taskId(1L)
                .title("Test Task")
                .build();

        kafkaProducerService.sendTaskEvent(event);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(kafkaTemplate).send(eq("task-events"), eq("1"), eventCaptor.capture());

        TaskEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(capturedEvent.getEventId()).isEqualTo("task-event-1");
    }

    @Test
    void sendTaskEvent_SetsIpAddressFromXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

        TaskEvent event = TaskEvent.builder()
                .eventId("task-event-2")
                .eventType("TASK_UPDATED")
                .taskId(2L)
                .build();

        kafkaProducerService.sendTaskEvent(event);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(kafkaTemplate).send(eq("task-events"), eq("2"), eventCaptor.capture());

        assertThat(eventCaptor.getValue().getIpAddress()).isEqualTo("192.168.1.100");
    }

    @Test
    void sendUserEvent_ValidEvent_SendsToCorrectTopic() {
        UserEvent event = UserEvent.builder()
                .eventId("user-event-1")
                .eventType("USER_REGISTERED")
                .userId(1L)
                .username("newuser")
                .build();

        kafkaProducerService.sendUserEvent(event);

        ArgumentCaptor<UserEvent> eventCaptor = ArgumentCaptor.forClass(UserEvent.class);
        verify(kafkaTemplate).send(eq("user-events"), eq("1"), eventCaptor.capture());

        UserEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getIdAddress()).isEqualTo("127.0.0.1");
        assertThat(capturedEvent.getUsername()).isEqualTo("newuser");
    }

    @Test
    void sendCommentEvent_ValidEvent_SendsToCorrectTopic() {
        CommentEvent event = CommentEvent.builder()
                .eventId("comment-event-1")
                .eventType("COMMENT_ADDED")
                .commentId(5L)
                .text("Great work")
                .build();

        kafkaProducerService.sendCommentEvent(event);

        ArgumentCaptor<CommentEvent> eventCaptor = ArgumentCaptor.forClass(CommentEvent.class);
        verify(kafkaTemplate).send(eq("comment-events"), eq("5"), eventCaptor.capture());

        CommentEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(capturedEvent.getText()).isEqualTo("Great work");
    }

    @Test
    void sendNotification_ValidEvent_SendsToCorrectTopic() {
        NotificationEvent event = NotificationEvent.builder()
                .notificationId("notif-1")
                .type("TASK_ASSIGNED")
                .userId(3L)
                .title("New task")
                .message("You have a new task")
                .build();

        kafkaProducerService.sendNotification(event);

        verify(kafkaTemplate).send(eq("notifications"), eq("3"), eq(event));
    }

    @Test
    void sendTaskEvent_EmptyXForwardedFor_UsesRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        TaskEvent event = TaskEvent.builder()
                .eventId("task-event-3")
                .eventType("TASK_DELETED")
                .taskId(3L)
                .build();

        kafkaProducerService.sendTaskEvent(event);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

        assertThat(eventCaptor.getValue().getIpAddress()).isEqualTo("10.0.0.1");
    }
}