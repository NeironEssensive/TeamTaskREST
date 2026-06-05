package com.example.team_task.dto.kafka;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String notificationId;
    private String type;             
    private Long userId;             
    private String title;
    private String message;
    private Long taskId;             
    private Long triggeredBy;       
    private LocalDateTime createdAt;
}
