package com.example.team_task.service;

import com.example.team_task.dto.kafka.CommentEvent;
import com.example.team_task.dto.kafka.NotificationEvent;
import com.example.team_task.dto.kafka.TaskEvent;
import com.example.team_task.dto.kafka.UserEvent;
import com.example.team_task.entity.Comment;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventPublisherServiceTest {

    @Mock
    private KafkaProducerService kafkaProducer;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private EventPublisherService eventPublisherService;

    private User taskOwner;
    private User performedBy;
    private Task testTask;

    @BeforeEach
    void setUp() {
        taskOwner = new User();
        taskOwner.setId(1L);
        taskOwner.setName("taskowner");

        performedBy = new User();
        performedBy.setId(2L);
        performedBy.setName("performer");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(Task.Status.PENDING);
        testTask.setPriority(Task.Priority.MEDIUM);
        testTask.setUser(taskOwner);
    }

    @Test
    void publishTaskCreated_OwnerCreatesTask_PublishesTaskEventOnly() {
        eventPublisherService.publishTaskCreated(testTask, taskOwner);

        ArgumentCaptor<TaskEvent> taskEventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(kafkaProducer).sendTaskEvent(taskEventCaptor.capture());
        verify(kafkaProducer, never()).sendNotification(any());

        TaskEvent event = taskEventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo("TASK_CREATED");
        assertThat(event.getTaskId()).isEqualTo(1L);
        assertThat(event.getTitle()).isEqualTo("Test Task");
        assertThat(event.getStatus()).isEqualTo("PENDING");
        assertThat(event.getPriority()).isEqualTo("MEDIUM");
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getPerformedBy()).isEqualTo(1L);
    }

    @Test
    void publishTaskCreated_AdminCreatesTaskForUser_PublishesTaskEventAndNotification() {
        eventPublisherService.publishTaskCreated(testTask, performedBy);

        verify(kafkaProducer).sendTaskEvent(any(TaskEvent.class));
        verify(kafkaProducer).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void publishTaskUpdated_StatusChanged_PublishesTaskEventAndStatusNotification() {
        Task taskAfter = new Task();
        taskAfter.setId(1L);
        taskAfter.setTitle("Test Task");
        taskAfter.setDescription("Test Description");
        taskAfter.setStatus(Task.Status.IN_PROGRESS);
        taskAfter.setPriority(Task.Priority.MEDIUM);
        taskAfter.setUser(taskOwner);

        eventPublisherService.publishTaskUpdated(testTask, taskAfter, performedBy);

        verify(kafkaProducer).sendTaskEvent(any(TaskEvent.class));
        verify(kafkaProducer).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void publishTaskUpdated_PriorityChanged_PublishesTaskEventAndPriorityNotification() {
        Task taskAfter = new Task();
        taskAfter.setId(1L);
        taskAfter.setTitle("Test Task");
        taskAfter.setDescription("Test Description");
        taskAfter.setStatus(Task.Status.PENDING);
        taskAfter.setPriority(Task.Priority.HIGH);
        taskAfter.setUser(taskOwner);

        eventPublisherService.publishTaskUpdated(testTask, taskAfter, performedBy);

        verify(kafkaProducer).sendTaskEvent(any(TaskEvent.class));
        verify(kafkaProducer).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void publishTaskUpdated_TitleChanged_PublishesTaskEvent() {
        Task taskAfter = new Task();
        taskAfter.setId(1L);
        taskAfter.setTitle("Updated Title");
        taskAfter.setDescription("Test Description");
        taskAfter.setStatus(Task.Status.PENDING);
        taskAfter.setPriority(Task.Priority.MEDIUM);
        taskAfter.setUser(taskOwner);

        eventPublisherService.publishTaskUpdated(testTask, taskAfter, performedBy);

        ArgumentCaptor<TaskEvent> taskEventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(kafkaProducer).sendTaskEvent(taskEventCaptor.capture());

        TaskEvent event = taskEventCaptor.getValue();
        assertThat(event.getChanges()).containsKey("title");
        assertThat(event.getChanges().get("title").getOldValue()).isEqualTo("Test Task");
        assertThat(event.getChanges().get("title").getNewValue()).isEqualTo("Updated Title");
    }

    @Test
    void publishTaskUpdated_NoChanges_DoesNotPublishEvent() {
        Task taskAfter = new Task();
        taskAfter.setId(1L);
        taskAfter.setTitle("Test Task");
        taskAfter.setDescription("Test Description");
        taskAfter.setStatus(Task.Status.PENDING);
        taskAfter.setPriority(Task.Priority.MEDIUM);
        taskAfter.setUser(taskOwner);

        eventPublisherService.publishTaskUpdated(testTask, taskAfter, performedBy);

        verify(kafkaProducer, never()).sendTaskEvent(any());
        verify(kafkaProducer, never()).sendNotification(any());
    }

    @Test
    void publishTaskUpdated_MultipleFieldsChanged_DetectsAllChanges() {
        Task taskAfter = new Task();
        taskAfter.setId(1L);
        taskAfter.setTitle("New Title");
        taskAfter.setDescription("New Description");
        taskAfter.setStatus(Task.Status.DONE);
        taskAfter.setPriority(Task.Priority.LOW);
        taskAfter.setUser(taskOwner);

        eventPublisherService.publishTaskUpdated(testTask, taskAfter, performedBy);

        ArgumentCaptor<TaskEvent> taskEventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(kafkaProducer).sendTaskEvent(taskEventCaptor.capture());

        TaskEvent event = taskEventCaptor.getValue();
        assertThat(event.getChanges()).hasSize(4);
        assertThat(event.getChanges()).containsKeys("title", "description", "status", "priority");
    }

    @Test
    void publishTaskDeleted_OwnerDeletesTask_PublishesTaskEventOnly() {
        eventPublisherService.publishTaskDeleted(testTask, taskOwner);

        verify(kafkaProducer).sendTaskEvent(any(TaskEvent.class));
        verify(kafkaProducer, never()).sendNotification(any());
    }

    @Test
    void publishTaskDeleted_AdminDeletesTask_PublishesTaskEventAndNotification() {
        eventPublisherService.publishTaskDeleted(testTask, performedBy);

        verify(kafkaProducer).sendTaskEvent(any(TaskEvent.class));
        verify(kafkaProducer).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void publishCommentAdded_OwnerComments_PublishesCommentEventOnly() {
        Comment comment = createComment(taskOwner);

        eventPublisherService.publishCommentAdded(comment, taskOwner);

        verify(kafkaProducer).sendCommentEvent(any(CommentEvent.class));
        verify(kafkaProducer, never()).sendNotification(any());
    }

    @Test
    void publishCommentAdded_OtherUserComments_PublishesCommentEventAndNotification() {
        Comment comment = createComment(taskOwner);

        eventPublisherService.publishCommentAdded(comment, performedBy);

        verify(kafkaProducer).sendCommentEvent(any(CommentEvent.class));
        verify(kafkaProducer).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void publishCommentDeleted_ValidComment_PublishesCommentEvent() {
        Comment comment = createComment(taskOwner);

        eventPublisherService.publishCommentDeleted(comment, performedBy);

        ArgumentCaptor<CommentEvent> commentEventCaptor = ArgumentCaptor.forClass(CommentEvent.class);
        verify(kafkaProducer).sendCommentEvent(commentEventCaptor.capture());

        CommentEvent event = commentEventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo("COMMENT_DELETED");
        assertThat(event.getCommentId()).isEqualTo(1L);
    }

    @Test
    void publishUserRegistered_ValidUser_PublishesUserEvent() {
        eventPublisherService.publishUserRegistered(taskOwner);

        ArgumentCaptor<UserEvent> userEventCaptor = ArgumentCaptor.forClass(UserEvent.class);
        verify(kafkaProducer).sendUserEvent(userEventCaptor.capture());

        UserEvent event = userEventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo("USER_REGISTERED");
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getUsername()).isEqualTo("taskowner");
        assertThat(event.getEmail()).isEqualTo("taskowner@example.com");
    }

    @Test
    void publishRoleChanged_ValidRoleChange_PublishesUserEventAndNotification() {
        eventPublisherService.publishRoleChanged(taskOwner, "USER", "ADMIN", performedBy);

        verify(kafkaProducer).sendUserEvent(any(UserEvent.class));
        verify(kafkaProducer).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void publishUserDeleted_ValidUser_PublishesUserEvent() {
        eventPublisherService.publishUserDeleted(taskOwner, performedBy);

        ArgumentCaptor<UserEvent> userEventCaptor = ArgumentCaptor.forClass(UserEvent.class);
        verify(kafkaProducer).sendUserEvent(userEventCaptor.capture());

        UserEvent event = userEventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo("USER_DELETED");
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getPerformedBy()).isEqualTo(2L);
    }

    private Comment createComment(User author) {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");
        comment.setTask(testTask);
        comment.setUser(author);
        return comment;
    }
}