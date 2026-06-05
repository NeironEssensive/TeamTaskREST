package com.example.team_task.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.team_task.dto.kafka.CommentEvent;
import com.example.team_task.dto.kafka.NotificationEvent;
import com.example.team_task.dto.kafka.TaskEvent;
import com.example.team_task.dto.kafka.UserEvent;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final HttpServletRequest request;


    public void sendTaskEvent(TaskEvent event){
        event.setIpAddress(getClientIp());
        kafkaTemplate.send("task-events", event.getTaskId().toString(), event);
    }
    public void sendUserEvent(UserEvent event){
        event.setIdAddress(getClientIp());
        kafkaTemplate.send("user-events", event.getUserId().toString(), event);
    }
    public void sendCommentEvent(CommentEvent event){
        event.setIpAddress(getClientIp());
        kafkaTemplate.send("comment-events", event.getCommentId().toString(), event);
    }
     public void sendNotification(NotificationEvent event) {
        kafkaTemplate.send("notifications", event.getUserId().toString(), event);
    }
    
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isEmpty()) ? ip : request.getRemoteAddr();
    }
}
