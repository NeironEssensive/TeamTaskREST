package com.example.team_task.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.team_task.entity.AuditLog;
import com.example.team_task.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditLogConsumer {
    private final AuditLogRepository auditLogRepository;
    @KafkaListener(topics = "audit-log", groupId = "teamtask-audit")
    public void consume(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }
}
