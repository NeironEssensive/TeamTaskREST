package com.example.team_task.dto.kafka;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User's notifications")
public class NotificationResponse {

    @Schema(description = "UUID notification", example = "a1b2c3d4-e5f6-7890")
    private String notificationId;

    @Schema(description = "notification type", example = "TASK_STATUS_CHANGED")
    private String type;

    @Schema(description = "title", example = "Task status changed")
    private String title;

    @Schema(description = "text", example = "Task 'Fix bug' changed from PENDING to IN_PROGRESS")
    private String message;

    @Schema(description = "task id, may be null", example = "5")
    private Long taskId;

    @Schema(description = "user id, triggered notification", example = "2")
    private Long triggeredBy;

    @Schema(description = "isRead???", example = "false")
    private boolean isRead;

    @Schema(description = "date create")
    private LocalDateTime createdAt;
}