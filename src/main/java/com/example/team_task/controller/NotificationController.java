package com.example.team_task.controller;


import com.example.team_task.dto.kafka.NotificationResponse;
import com.example.team_task.dto.kafka.UnreadCountResponse;
import com.example.team_task.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Control user notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "get all notifications current user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notifications list"),
        @ApiResponse(responseCode = "401", description = "Unautorized")
    })
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "get count unread notifications")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "count unread"),
        @ApiResponse(responseCode = "401", description = "unautorized")
    })
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "mark notifications as read")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "notification marked as read"),
        @ApiResponse(responseCode = "401", description = "unautorized"),
        @ApiResponse(responseCode = "403", description = "alien notification"),
        @ApiResponse(responseCode = "404", description = "notification not found")
    })
    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Отметить все уведомления как прочитанные")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "all notifications marked as read"),
        @ApiResponse(responseCode = "401", description = "unautorized")
    })
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Удалить уведомление")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "notification marked as read"),
        @ApiResponse(responseCode = "401", description = "unautorized"),
        @ApiResponse(responseCode = "403", description = "alien notification"),
        @ApiResponse(responseCode = "404", description = "notification not found")
    })
    public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
