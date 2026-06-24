package com.example.team_task.service;

import com.example.team_task.dto.kafka.AuditLogResponse;
import com.example.team_task.entity.AuditLog;
import com.example.team_task.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLog testAuditLog;
    private LocalDateTime from;
    private LocalDateTime to;

    @BeforeEach
    void setUp() {
        from = LocalDateTime.of(2026, 6, 1, 0, 0);
        to = LocalDateTime.of(2026, 6, 23, 23, 59);

        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setAuditId("audit-uuid-123");
        testAuditLog.setAction("TASK_CREATED");
        testAuditLog.setEntityType("TASK");
        testAuditLog.setEntityId(42L);
        testAuditLog.setPerformedBy(2L);
        testAuditLog.setPerformedByName("admin");
        testAuditLog.setDetails("Task created by admin");
        testAuditLog.setIpAddress("192.168.1.1");
        testAuditLog.setCreatedAt(LocalDateTime.of(2026, 6, 15, 10, 30));
    }

    @Test
    void getAuditLog_WithUserIdAndDateRange_UsesCorrectRepositoryMethod() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<AuditLog> auditPage = new PageImpl<>(List.of(testAuditLog), pageRequest, 1);

        when(auditLogRepository.findByPerformedByAndCreatedAtBetween(2L, from, to, pageRequest))
                .thenReturn(auditPage);

        Page<AuditLogResponse> result = auditLogService.getAuditLog(
                from, to, null, 2L, null, 0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuditId()).isEqualTo("audit-uuid-123");
        verify(auditLogRepository).findByPerformedByAndCreatedAtBetween(2L, from, to, pageRequest);
    }

    @Test
    void getAuditLog_WithDateRangeOnly_UsesCorrectRepositoryMethod() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<AuditLog> auditPage = new PageImpl<>(List.of(testAuditLog), pageRequest, 1);

        when(auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pageRequest))
                .thenReturn(auditPage);

        Page<AuditLogResponse> result = auditLogService.getAuditLog(
                from, to, null, null, null, 0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(auditLogRepository).findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pageRequest);
    }

    @Test
    void getAuditLog_WithUserIdOnly_UsesCorrectRepositoryMethod() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<AuditLog> auditPage = new PageImpl<>(List.of(testAuditLog), pageRequest, 1);

        when(auditLogRepository.findByPerformedBy(2L, pageRequest)).thenReturn(auditPage);

        Page<AuditLogResponse> result = auditLogService.getAuditLog(
                null, null, null, 2L, null, 0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(auditLogRepository).findByPerformedBy(2L, pageRequest);
    }

    @Test
    void getAuditLog_WithNoFilters_UsesFindAllMethod() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<AuditLog> auditPage = new PageImpl<>(List.of(testAuditLog), pageRequest, 1);

        when(auditLogRepository.findAllByOrderByCreatedAtDesc(pageRequest)).thenReturn(auditPage);

        Page<AuditLogResponse> result = auditLogService.getAuditLog(
                null, null, null, null, null, 0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(auditLogRepository).findAllByOrderByCreatedAtDesc(pageRequest);
    }

    @Test
    void getAuditLog_EmptyResult_ReturnsEmptyPage() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<AuditLog> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(auditLogRepository.findAllByOrderByCreatedAtDesc(pageRequest)).thenReturn(emptyPage);

        Page<AuditLogResponse> result = auditLogService.getAuditLog(
                null, null, null, null, null, 0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getAuditLog_DifferentPageSize_UsesProvidedPagination() {
        PageRequest pageRequest = PageRequest.of(1, 10);
        Page<AuditLog> auditPage = new PageImpl<>(List.of(testAuditLog), pageRequest, 15);

        when(auditLogRepository.findAllByOrderByCreatedAtDesc(pageRequest)).thenReturn(auditPage);

        Page<AuditLogResponse> result = auditLogService.getAuditLog(
                null, null, null, null, null, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(15);
    }

    @Test
    void getAuditLog_WithUserIdAndDateRange_PrioritizesCombinedFilter() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<AuditLog> auditPage = new PageImpl<>(List.of(testAuditLog), pageRequest, 1);

        when(auditLogRepository.findByPerformedByAndCreatedAtBetween(5L, from, to, pageRequest))
                .thenReturn(auditPage);

        auditLogService.getAuditLog(from, to, "TASK_CREATED", 5L, "TASK", 0, 20);

        verify(auditLogRepository).findByPerformedByAndCreatedAtBetween(5L, from, to, pageRequest);
        verify(auditLogRepository, never()).findByCreatedAtBetweenOrderByCreatedAtDesc(any(), any(), any());
        verify(auditLogRepository, never()).findByPerformedBy(any(), any());
        verify(auditLogRepository, never()).findAllByOrderByCreatedAtDesc(any());
    }

    @Test
    void mapToResponse_ValidAuditLog_ReturnsCorrectResponse() {
        AuditLogResponse response = auditLogService.getAuditLog(
                from, to, null, 2L, null, 0, 20)
                .getContent().isEmpty() ? null : null;

        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<AuditLog> auditPage = new PageImpl<>(List.of(testAuditLog), pageRequest, 1);
        when(auditLogRepository.findByPerformedByAndCreatedAtBetween(2L, from, to, pageRequest))
                .thenReturn(auditPage);

        Page<AuditLogResponse> result = auditLogService.getAuditLog(
                from, to, null, 2L, null, 0, 20);

        AuditLogResponse firstResponse = result.getContent().get(0);
        assertThat(firstResponse.getAuditId()).isEqualTo("audit-uuid-123");
        assertThat(firstResponse.getAction()).isEqualTo("TASK_CREATED");
        assertThat(firstResponse.getEntityType()).isEqualTo("TASK");
        assertThat(firstResponse.getEntityId()).isEqualTo(42L);
        assertThat(firstResponse.getPerformedBy()).isEqualTo(2L);
        assertThat(firstResponse.getPerformedByName()).isEqualTo("admin");
        assertThat(firstResponse.getDetails()).isEqualTo("Task created by admin");
        assertThat(firstResponse.getIpAddress()).isEqualTo("192.168.1.1");
    }
}