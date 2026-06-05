package com.example.team_task.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "audit_id", nullable = false, unique = true, length = 36)
    private String auditId;
    @Column(nullable = false, length = 50)
    private String action;
    @Column(name = "entity_type", nullable = false, length = 30)
    private String entityType;
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    @Column(name = "performed_by")
    private Long performedBy;
    @Column(name = "performed_by_name", length = 30)
    private String performedByName;
    @Column(columnDefinition = "TEXT")
    private String details;
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
