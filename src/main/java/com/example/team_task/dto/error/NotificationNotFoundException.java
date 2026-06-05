package com.example.team_task.dto.error;

public class NotificationNotFoundException extends RuntimeException{
    public NotificationNotFoundException(String notificationId) {
        super("Notification with id '" + notificationId + "' not found");
    }
}
