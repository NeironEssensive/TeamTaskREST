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
@Schema(description = "logging audit-log")
public class AuditLogResponse {

    @Schema(description = "UUID log", example = "a1b2c3d4-e5f6-7890")
    private String auditId;

    @Schema(description = "Action", example = "TASK_CREATED")
    private String action;

    @Schema(description = "Entity type", example = "TASK")
    private String entityType;

    @Schema(description = "Entity id", example = "1")
    private Long entityId;

    @Schema(description = "Performed id", example = "2")
    private Long performedBy;

    @Schema(description = "Performed name", example = "admin")
    private String performedByName;

    @Schema(description = "Details")
    private String details;

    @Schema(description = "Old values")
    private String oldValues;

    @Schema(description = "New values")
    private String newValues;

    @Schema(description = "IP-address", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "date create")
    private LocalDateTime createdAt;
}
