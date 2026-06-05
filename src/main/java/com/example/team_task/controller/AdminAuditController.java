package com.example.team_task.controller;

import com.example.team_task.dto.kafka.AuditLogResponse;
import com.example.team_task.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@Tag(name = "Admin Audit", description = "look audit-logs (only for admin)")
@SecurityRequirement(name = "bearerAuth")
public class AdminAuditController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "get filter audit-log")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "audit-log's page"),
        @ApiResponse(responseCode = "401", description = "not autorized"),
        @ApiResponse(responseCode = "403", description = "required role admin")
    })
    public ResponseEntity<Page<AuditLogResponse>> getAuditLog(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Schema(description = "Start period", example = "2026-06-01T00:00:00")
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Schema(description = "End period", example = "2026-06-05T23:59:59")
            LocalDateTime to,

            @RequestParam(required = false)
            @Schema(description = "Action type", example = "TASK_CREATED")
            String action,

            @RequestParam(required = false)
            @Schema(description = "ID user, performed by", example = "2")
            Long userId,

            @RequestParam(required = false)
            @Schema(description = "Entity type", example = "TASK")
            String entityType,

            @RequestParam(defaultValue = "0")
            @Schema(description = "Page number", example = "0")
            int page,

            @RequestParam(defaultValue = "20")
            @Schema(description = "Page size", example = "20")
            int size) {

        return ResponseEntity.ok(
                auditLogService.getAuditLog(from, to, action, userId, entityType, page, size));
    }
}