package com.example.team_task.repository;

import com.example.team_task.entity.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuditLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2026, 6, 15, 10, 0);
    }

    @Test
    void findByPerformedByAndCreatedAtBetween_ValidParams_ReturnsMatchingLogs() {
        AuditLog log1 = createAuditLog("audit-1", 2L, baseTime.plusHours(1));
        AuditLog log2 = createAuditLog("audit-2", 2L, baseTime.plusHours(2));
        AuditLog log3 = createAuditLog("audit-3", 3L, baseTime.plusHours(3));

        entityManager.persistAndFlush(log1);
        entityManager.persistAndFlush(log2);
        entityManager.persistAndFlush(log3);

        Page<AuditLog> result = auditLogRepository.findByPerformedByAndCreatedAtBetween(
                2L, baseTime, baseTime.plusHours(3), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(AuditLog::getAuditId)
                .containsExactlyInAnyOrder("audit-1", "audit-2");
    }

    @Test
    void findByPerformedByAndCreatedAtBetween_NoMatchingLogs_ReturnsEmptyPage() {
        Page<AuditLog> result = auditLogRepository.findByPerformedByAndCreatedAtBetween(
                99L, baseTime, baseTime.plusHours(5), PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByCreatedAtBetweenOrderByCreatedAtDesc_ValidRange_ReturnsOrderedLogs() {
        AuditLog log1 = createAuditLog("audit-1", 2L, baseTime.plusHours(1));
        AuditLog log2 = createAuditLog("audit-2", 3L, baseTime.plusHours(3));

        entityManager.persistAndFlush(log1);
        entityManager.persistAndFlush(log2);

        Page<AuditLog> result = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                baseTime, baseTime.plusHours(5), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getAuditId()).isEqualTo("audit-2");
        assertThat(result.getContent().get(1).getAuditId()).isEqualTo("audit-1");
    }

    @Test
    void findByCreatedAtBetweenOrderByCreatedAtDesc_OutsideRange_ReturnsEmpty() {
        AuditLog log = createAuditLog("audit-1", 2L, baseTime.plusHours(1));
        entityManager.persistAndFlush(log);

        LocalDateTime outsideRange = baseTime.plusDays(10);
        Page<AuditLog> result = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                outsideRange, outsideRange.plusHours(1), PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByPerformedBy_ExistingUser_ReturnsUserLogs() {
        AuditLog log1 = createAuditLog("audit-1", 2L, baseTime);
        AuditLog log2 = createAuditLog("audit-2", 2L, baseTime.plusHours(1));
        AuditLog log3 = createAuditLog("audit-3", 5L, baseTime.plusHours(2));

        entityManager.persistAndFlush(log1);
        entityManager.persistAndFlush(log2);
        entityManager.persistAndFlush(log3);

        Page<AuditLog> result = auditLogRepository.findByPerformedBy(2L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findByPerformedBy_NonExistentUser_ReturnsEmptyPage() {
        Page<AuditLog> result = auditLogRepository.findByPerformedBy(99L, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByOrderByCreatedAtDesc_MultipleLogs_ReturnsAllOrderedByCreatedAtDesc() {
        AuditLog log1 = createAuditLog("audit-1", 2L, baseTime.plusHours(1));
        AuditLog log2 = createAuditLog("audit-2", 3L, baseTime.plusHours(3));
        AuditLog log3 = createAuditLog("audit-3", 4L, baseTime.plusHours(2));

        entityManager.persistAndFlush(log1);
        entityManager.persistAndFlush(log2);
        entityManager.persistAndFlush(log3);

        Page<AuditLog> result = auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getAuditId()).isEqualTo("audit-2");
        assertThat(result.getContent().get(1).getAuditId()).isEqualTo("audit-3");
        assertThat(result.getContent().get(2).getAuditId()).isEqualTo("audit-1");
    }

    @Test
    void findAllByOrderByCreatedAtDesc_WithPagination_RespectsPageSize() {
        AuditLog log1 = createAuditLog("audit-1", 2L, baseTime.plusHours(1));
        AuditLog log2 = createAuditLog("audit-2", 3L, baseTime.plusHours(2));
        AuditLog log3 = createAuditLog("audit-3", 4L, baseTime.plusHours(3));

        entityManager.persistAndFlush(log1);
        entityManager.persistAndFlush(log2);
        entityManager.persistAndFlush(log3);

        Page<AuditLog> result = auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 2));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    private AuditLog createAuditLog(String auditId, Long performedBy, LocalDateTime createdAt) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditId(auditId);
        auditLog.setAction("TASK_CREATED");
        auditLog.setEntityType("TASK");
        auditLog.setEntityId(42L);
        auditLog.setPerformedBy(performedBy);
        auditLog.setPerformedByName("user-" + performedBy);
        auditLog.setDetails("Details for " + auditId);
        auditLog.setIpAddress("192.168.1.1");
        auditLog.setCreatedAt(createdAt);
        return auditLog;
    }
}