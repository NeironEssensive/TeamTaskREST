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
public class CommentEvent {
    private String eventId;
    private String eventType;       
    private Long commentId;
    private String text;
    private Long taskId;
    private Long userId;             
    private String userName;
    private LocalDateTime timestamp;
    private String ipAddress;
}
