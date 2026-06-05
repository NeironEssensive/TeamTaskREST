package com.example.team_task.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.team_task.dto.kafka.NotificationEvent;
import com.example.team_task.entity.Notification;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.repository.NotificationRepository;
import com.example.team_task.repository.TaskRepository;
import com.example.team_task.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @KafkaListener(topics = "notifications", groupId = "teamtask-notifications")
    public void consume(NotificationEvent event) {
        User user = userRepository.findById(event.getUserId())
                .orElse(null);
        if (user == null) return;

        Task task = null;
        if (event.getTaskId() != null) {
            task = taskRepository.findById(event.getTaskId()).orElse(null);
        }

        Notification notification = new Notification();
        notification.setNotificationId(event.getNotificationId());
        notification.setUser(user);
        notification.setType(event.getType());
        notification.setTitle(event.getTitle());
        notification.setMessage(event.getMessage());
        notification.setTask(task);
        notification.setTriggeredBy(event.getTriggeredBy());
        notification.setCreatedAt(event.getCreatedAt());
        notificationRepository.save(notification);
}
}
