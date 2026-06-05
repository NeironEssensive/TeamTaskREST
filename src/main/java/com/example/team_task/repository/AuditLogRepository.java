package com.example.team_task.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.team_task.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>{
   Page<AuditLog> findByPerformedByAndCreatedAtBetween(
            Long userId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByPerformedBy(Long userId, Pageable pageable);

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
