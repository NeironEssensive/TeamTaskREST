package com.example.team_task.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Task entity representing a task in the system")
public class Task {
    
    @Schema(description = "Task status enumeration")
    public enum Status {
        PENDING, IN_PROGRESS, DONE
    }

    @Schema(description = "Task priority enumeration")
    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the task", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "Title of the task", example = "Implement user authentication", required = true, minLength = 1, maxLength = 100)
    private String title;

    @Column(length = 500)
    @Schema(description = "Detailed description of the task", example = "Implement JWT-based authentication for the API", maxLength = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Current status of the task", example = "PENDING", required = true, allowableValues = {"PENDING", "IN_PROGRESS", "DONE"})
    private Status status = Status.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Priority level of the task", example = "HIGH", required = true, allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private Priority priority = Priority.MEDIUM;

    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when the task was created", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Schema(description = "Timestamp when the task was last updated", example = "2024-01-15T14:45:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"tasks", "comments", "password"})
    @Schema(description = "User who owns this task", required = true)
    private User user;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "List of comments on this task", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Task(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
    }
}