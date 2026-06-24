package com.example.team_task.kafka;

import com.example.team_task.entity.AuditLog;
import com.example.team_task.repository.AuditLogRepository;
import com.example.team_task.service.AuditLogConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogConsumerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogConsumer auditLogConsumer;

    @Test
    void consume_ValidAuditLog_SavesToRepository() {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditId("audit-123");
        auditLog.setAction("TASK_CREATED");
        auditLog.setEntityType("TASK");
        auditLog.setEntityId(1L);
        auditLog.setPerformedBy(2L);

        auditLogConsumer.consume(auditLog);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertThat(savedLog.getAuditId()).isEqualTo("audit-123");
        assertThat(savedLog.getAction()).isEqualTo("TASK_CREATED");
        assertThat(savedLog.getEntityType()).isEqualTo("TASK");
        assertThat(savedLog.getEntityId()).isEqualTo(1L);
        assertThat(savedLog.getPerformedBy()).isEqualTo(2L);
    }

    @Test
    void consume_MultipleAuditLogs_SavesAllToRepository() {
        AuditLog firstLog = new AuditLog();
        firstLog.setAuditId("audit-1");
        firstLog.setAction("USER_REGISTERED");

        AuditLog secondLog = new AuditLog();
        secondLog.setAuditId("audit-2");
        secondLog.setAction("USER_DELETED");

        auditLogConsumer.consume(firstLog);
        auditLogConsumer.consume(secondLog);

        verify(auditLogRepository).save(firstLog);
        verify(auditLogRepository).save(secondLog);
    }
}