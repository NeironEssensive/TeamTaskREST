package com.example.team_task.service;

import com.example.team_task.dto.kafka.AuditLogResponse;
import com.example.team_task.entity.AuditLog;
import com.example.team_task.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public Page<AuditLogResponse> getAuditLog(LocalDateTime from, LocalDateTime to,
                                               String action, Long userId,
                                               String entityType, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<AuditLog> auditLogs;

        if (userId != null && from != null && to != null) {
            auditLogs = auditLogRepository.findByPerformedByAndCreatedAtBetween(
                    userId, from, to, pageRequest);
        } else if (from != null && to != null) {
            auditLogs = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                    from, to, pageRequest);
        } else if (userId != null) {
            auditLogs = auditLogRepository.findByPerformedBy(userId, pageRequest);
        } else {
            auditLogs = auditLogRepository.findAllByOrderByCreatedAtDesc(pageRequest);
        }

        return auditLogs.map(this::mapToResponse);
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .auditId(auditLog.getAuditId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .performedBy(auditLog.getPerformedBy())
                .performedByName(auditLog.getPerformedByName())
                .details(auditLog.getDetails())
                .oldValues(auditLog.getOldValues())
                .newValues(auditLog.getNewValues())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
