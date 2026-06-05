package com.example.team_task.service;

import com.example.team_task.dto.kafka.CommentEvent;
import com.example.team_task.dto.kafka.FieldChange;
import com.example.team_task.dto.kafka.NotificationEvent;
import com.example.team_task.dto.kafka.TaskEvent;
import com.example.team_task.dto.kafka.UserEvent;
import com.example.team_task.dto.task.TaskResponse;
import com.example.team_task.entity.Comment;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final KafkaProducerService kafkaProducer;
    private final HttpServletRequest request;

    public void publishTaskCreated(Task task, User performedBy) {
        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TASK_CREATED")
                .taskId(task.getId())
                .title(task.getTitle())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .userId(task.getUser().getId())
                .performedBy(performedBy.getId())
                .performedByName(performedBy.getName())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducer.sendTaskEvent(event);

        if (!task.getUser().getId().equals(performedBy.getId())) {
            sendTaskNotification(task.getUser().getId(), task.getId(), performedBy,
                    "TASK_ASSIGNED",
                    "New task assigned",
                    "Admin " + performedBy.getName() + " assigned you a task: '" + task.getTitle() + "'");
        }
    }

    public void publishTaskUpdated(Task taskBefore, Task taskAfter, User performedBy) {
        Map<String, FieldChange> changes = detectChanges(taskBefore, taskAfter);

        if (changes.isEmpty()) {
            return;
        }

        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TASK_UPDATED")
                .taskId(taskAfter.getId())
                .title(taskAfter.getTitle())
                .status(taskAfter.getStatus().name())
                .priority(taskAfter.getPriority().name())
                .userId(taskAfter.getUser().getId())
                .performedBy(performedBy.getId())
                .performedByName(performedBy.getName())
                .changes(changes)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducer.sendTaskEvent(event);

        if (changes.containsKey("status")) {
            FieldChange statusChange = changes.get("status");
            sendTaskNotification(taskAfter.getUser().getId(), taskAfter.getId(), performedBy,
                    "TASK_STATUS_CHANGED",
                    "Task status changed",
                    "Task '" + taskAfter.getTitle() + "' changed from " +
                            statusChange.getOldValue() + " to " + statusChange.getNewValue());
        }

        if (changes.containsKey("priority")) {
            FieldChange priorityChange = changes.get("priority");
            sendTaskNotification(taskAfter.getUser().getId(), taskAfter.getId(), performedBy,
                    "TASK_PRIORITY_CHANGED",
                    "Task priority changed",
                    "Task '" + taskAfter.getTitle() + "' priority changed from " +
                            priorityChange.getOldValue() + " to " + priorityChange.getNewValue());
        }
    }

 
    public void publishTaskDeleted(Task task, User performedBy) {
        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TASK_DELETED")
                .taskId(task.getId())
                .title(task.getTitle())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .userId(task.getUser().getId())
                .performedBy(performedBy.getId())
                .performedByName(performedBy.getName())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducer.sendTaskEvent(event);
        if (!task.getUser().getId().equals(performedBy.getId())) {
            sendTaskNotification(task.getUser().getId(), task.getId(), performedBy,
                    "TASK_DELETED",
                    "Task deleted",
                    "Admin " + performedBy.getName() + " deleted your task: '" + task.getTitle() + "'");
        }
    }

    public void publishCommentAdded(Comment comment, User performedBy) {
        CommentEvent event = CommentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("COMMENT_ADDED")
                .commentId(comment.getId())
                .text(comment.getText())
                .taskId(comment.getTask().getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getName())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducer.sendCommentEvent(event);

        Task task = comment.getTask();
        if (!task.getUser().getId().equals(performedBy.getId())) {
            sendTaskNotification(task.getUser().getId(), task.getId(), performedBy,
                    "COMMENT_ADDED",
                    "New comment",
                    performedBy.getName() + " commented on task '" + task.getTitle() + "'");
        }
    }

    public void publishCommentDeleted(Comment comment, User performedBy) {
        CommentEvent event = CommentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("COMMENT_DELETED")
                .commentId(comment.getId())
                .text(comment.getText())
                .taskId(comment.getTask().getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getName())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducer.sendCommentEvent(event);
    }

    public void publishUserRegistered(User user) {
        UserEvent event = UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_REGISTERED")
                .userId(user.getId())
                .username(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducer.sendUserEvent(event);
    }

    public void publishRoleChanged(User targetUser, String oldRole, String newRole, User performedBy) {
        Map<String, String> changes = new HashMap<>();
        changes.put("role", oldRole + " -> " + newRole);

        UserEvent event = UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ROLE_CHANGED")
                .userId(targetUser.getId())
                .username(targetUser.getName())
                .email(targetUser.getEmail())
                .role(newRole)
                .performedBy(performedBy.getId())
                .performedByName(performedBy.getName())
                .changes(changes)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducer.sendUserEvent(event);

        sendUserNotification(targetUser.getId(), performedBy,
                "ROLE_CHANGED",
                "Your role has been changed",
                "Your role has been changed from " + oldRole + " to " + newRole);
    }

    public void publishUserDeleted(User deletedUser, User performedBy) {
        UserEvent event = UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_DELETED")
                .userId(deletedUser.getId())
                .username(deletedUser.getName())
                .email(deletedUser.getEmail())
                .role(deletedUser.getRole().name())
                .performedBy(performedBy.getId())
                .performedByName(performedBy.getName())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducer.sendUserEvent(event);
    }

    private Map<String, FieldChange> detectChanges(Task before, Task after) {
        Map<String, FieldChange> changes = new HashMap<>();

        if (!before.getTitle().equals(after.getTitle())) {
            changes.put("title", new FieldChange(before.getTitle(), after.getTitle()));
        }
        if (!before.getDescription().equals(after.getDescription())) {
            changes.put("description", new FieldChange(
                    before.getDescription() != null ? before.getDescription() : "",
                    after.getDescription() != null ? after.getDescription() : ""));
        }
        if (!before.getStatus().equals(after.getStatus())) {
            changes.put("status", new FieldChange(
                    before.getStatus().name(), after.getStatus().name()));
        }
        if (!before.getPriority().equals(after.getPriority())) {
            changes.put("priority", new FieldChange(
                    before.getPriority().name(), after.getPriority().name()));
        }
      
        return changes;
    }

    private void sendTaskNotification(Long userId, Long taskId, User performedBy,
                                       String type, String title, String message) {
        NotificationEvent notification = NotificationEvent.builder()
                .notificationId(UUID.randomUUID().toString())
                .type(type)
                .userId(userId)
                .title(title)
                .message(message)
                .taskId(taskId)
                .triggeredBy(performedBy.getId())
                .createdAt(LocalDateTime.now())
                .build();

        kafkaProducer.sendNotification(notification);
    }

    private void sendUserNotification(Long userId, User performedBy,
                                       String type, String title, String message) {
        NotificationEvent notification = NotificationEvent.builder()
                .notificationId(UUID.randomUUID().toString())
                .type(type)
                .userId(userId)
                .title(title)
                .message(message)
                .taskId(null)
                .triggeredBy(performedBy.getId())
                .createdAt(LocalDateTime.now())
                .build();

        kafkaProducer.sendNotification(notification);
    }
}
