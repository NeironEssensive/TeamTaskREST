package com.example.team_task.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.team_task.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>{
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
    Optional<Notification> findByNotificationId(String notificationId);
}
