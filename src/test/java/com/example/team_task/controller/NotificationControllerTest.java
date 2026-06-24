package com.example.team_task.controller;

import com.example.team_task.dto.error.AccessDeniedException;
import com.example.team_task.dto.error.NotificationNotFoundException;
import com.example.team_task.dto.kafka.NotificationResponse;
import com.example.team_task.dto.kafka.UnreadCountResponse;
import com.example.team_task.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        notificationResponse = NotificationResponse.builder()
                .notificationId("notif-uuid-123")
                .type("TASK_ASSIGNED")
                .title("New task assigned")
                .message("You have been assigned a new task")
                .taskId(10L)
                .triggeredBy(2L)
                .isRead(false)
                .createdAt(LocalDateTime.of(2026, 6, 15, 10, 30))
                .build();
    }

    @Test
    void getMyNotifications_UserHasNotifications_ReturnsList() throws Exception {
        when(notificationService.getMyNotifications()).thenReturn(List.of(notificationResponse));

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value("notif-uuid-123"))
                .andExpect(jsonPath("$[0].type").value("TASK_ASSIGNED"))
                .andExpect(jsonPath("$[0].title").value("New task assigned"))
                .andExpect(jsonPath("$[0].message").value("You have been assigned a new task"))
                .andExpect(jsonPath("$[0].taskId").value(10))
                .andExpect(jsonPath("$[0].triggeredBy").value(2))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void getMyNotifications_UserHasNoNotifications_ReturnsEmptyList() throws Exception {
        when(notificationService.getMyNotifications()).thenReturn(List.of());

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyNotifications_MultipleNotifications_ReturnsAll() throws Exception {
        NotificationResponse secondNotification = NotificationResponse.builder()
                .notificationId("notif-uuid-456")
                .type("COMMENT_ADDED")
                .title("New comment")
                .message("Someone commented on your task")
                .taskId(10L)
                .triggeredBy(3L)
                .isRead(true)
                .build();

        when(notificationService.getMyNotifications())
                .thenReturn(List.of(notificationResponse, secondNotification));

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value("notif-uuid-123"))
                .andExpect(jsonPath("$[1].notificationId").value("notif-uuid-456"))
                .andExpect(jsonPath("$[1].read").value(true));
    }

    @Test
    void getUnreadCount_UserHasUnread_ReturnsCount() throws Exception {
        UnreadCountResponse countResponse = UnreadCountResponse.builder()
                .unreadCount(5L)
                .build();

        when(notificationService.getUnreadCount()).thenReturn(countResponse);

        mockMvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(5));
    }

    @Test
    void getUnreadCount_NoUnreadNotifications_ReturnsZero() throws Exception {
        UnreadCountResponse countResponse = UnreadCountResponse.builder()
                .unreadCount(0L)
                .build();

        when(notificationService.getUnreadCount()).thenReturn(countResponse);

        mockMvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    void markAsRead_ValidNotification_ReturnsOk() throws Exception {
        mockMvc.perform(patch("/notifications/notif-uuid-123/read"))
                .andExpect(status().isOk());

        verify(notificationService).markAsRead("notif-uuid-123");
    }

    @Test
    void markAsRead_NotificationNotFound_ReturnsNotFound() throws Exception {
        doThrow(new NotificationNotFoundException("notif-999"))
                .when(notificationService).markAsRead("notif-999");

        mockMvc.perform(patch("/notifications/notif-999/read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("notification not found"));
    }

    @Test
    void markAsRead_AlienNotification_ReturnsForbidden() throws Exception {
        doThrow(new AccessDeniedException("You can only manage your own notifications"))
                .when(notificationService).markAsRead("notif-alien");

        mockMvc.perform(patch("/notifications/notif-alien/read"))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    @Test
    void markAllAsRead_ReturnsOk() throws Exception {
        mockMvc.perform(patch("/notifications/read-all"))
                .andExpect(status().isOk());

        verify(notificationService).markAllAsRead();
    }

    @Test
    void deleteNotification_ValidNotification_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/notifications/notif-uuid-123"))
                .andExpect(status().isNoContent());

        verify(notificationService).deleteNotification("notif-uuid-123");
    }

    @Test
    void deleteNotification_NotFound_ReturnsNotFound() throws Exception {
        doThrow(new NotificationNotFoundException("notif-999"))
                .when(notificationService).deleteNotification("notif-999");

        mockMvc.perform(delete("/notifications/notif-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNotification_AlienNotification_ReturnsForbidden() throws Exception {
        doThrow(new AccessDeniedException("You can only manage your own notifications"))
                .when(notificationService).deleteNotification("notif-alien");

        mockMvc.perform(delete("/notifications/notif-alien"))
                .andExpect(status().isLocked());
    }
}