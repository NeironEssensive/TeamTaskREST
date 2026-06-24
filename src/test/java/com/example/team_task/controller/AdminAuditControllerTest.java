package com.example.team_task.controller;

import com.example.team_task.dto.kafka.AuditLogResponse;
import com.example.team_task.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAuditController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditLogService auditLogService;

    private AuditLogResponse auditLogResponse;

    @BeforeEach
    void setUp() {
        auditLogResponse = AuditLogResponse.builder()
                .auditId("audit-uuid-123")
                .action("TASK_CREATED")
                .entityType("TASK")
                .entityId(42L)
                .performedBy(2L)
                .performedByName("admin")
                .details("Task created by admin")
                .ipAddress("192.168.1.1")
                .createdAt(LocalDateTime.of(2026, 6, 15, 10, 30))
                .build();
    }

    @Test
    void getAuditLog_NoFilters_ReturnsPagedResults() throws Exception {
        Page<AuditLogResponse> auditPage = new PageImpl<>(
                List.of(auditLogResponse), PageRequest.of(0, 20), 1);

        when(auditLogService.getAuditLog(isNull(), isNull(), isNull(), isNull(), isNull(), 0, 20))
                .thenReturn(auditPage);

        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].auditId").value("audit-uuid-123"))
                .andExpect(jsonPath("$.content[0].action").value("TASK_CREATED"))
                .andExpect(jsonPath("$.content[0].entityType").value("TASK"))
                .andExpect(jsonPath("$.content[0].entityId").value(42))
                .andExpect(jsonPath("$.content[0].performedBy").value(2))
                .andExpect(jsonPath("$.content[0].performedByName").value("admin"))
                .andExpect(jsonPath("$.content[0].details").value("Task created by admin"))
                .andExpect(jsonPath("$.content[0].ipAddress").value("192.168.1.1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAuditLog_WithDateRange_PassesDatesToService() throws Exception {
        Page<AuditLogResponse> emptyPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 20), 0);

        when(auditLogService.getAuditLog(
                any(LocalDateTime.class), any(LocalDateTime.class),
                isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/admin/audit")
                        .param("from", "2026-06-01T00:00:00")
                        .param("to", "2026-06-23T23:59:59"))
                .andExpect(status().isOk());
    }

    @Test
    void getAuditLog_WithUserId_PassesUserIdToService() throws Exception {
        Page<AuditLogResponse> auditPage = new PageImpl<>(
                List.of(auditLogResponse), PageRequest.of(0, 20), 1);

        when(auditLogService.getAuditLog(
                isNull(), isNull(), isNull(), eq(2L), isNull(), eq(0), eq(20)))
                .thenReturn(auditPage);

        mockMvc.perform(get("/admin/audit")
                        .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].performedBy").value(2));
    }

    @Test
    void getAuditLog_WithAction_PassesActionToService() throws Exception {
        Page<AuditLogResponse> auditPage = new PageImpl<>(
                List.of(auditLogResponse), PageRequest.of(0, 20), 1);

        when(auditLogService.getAuditLog(
                isNull(), isNull(), eq("TASK_CREATED"), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(auditPage);

        mockMvc.perform(get("/admin/audit")
                        .param("action", "TASK_CREATED"))
                .andExpect(status().isOk());
    }

    @Test
    void getAuditLog_WithEntityType_PassesEntityTypeToService() throws Exception {
        Page<AuditLogResponse> auditPage = new PageImpl<>(
                List.of(auditLogResponse), PageRequest.of(0, 20), 1);

        when(auditLogService.getAuditLog(
                isNull(), isNull(), isNull(), isNull(), eq("TASK"), eq(0), eq(20)))
                .thenReturn(auditPage);

        mockMvc.perform(get("/admin/audit")
                        .param("entityType", "TASK"))
                .andExpect(status().isOk());
    }

    @Test
    void getAuditLog_WithCustomPageAndSize_ReturnsCorrectPage() throws Exception {
        Page<AuditLogResponse> auditPage = new PageImpl<>(
                List.of(auditLogResponse), PageRequest.of(2, 10), 25);

        when(auditLogService.getAuditLog(
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(2), eq(10)))
                .thenReturn(auditPage);

        mockMvc.perform(get("/admin/audit")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(25));
    }

    @Test
    void getAuditLog_EmptyResult_ReturnsEmptyPage() throws Exception {
        Page<AuditLogResponse> emptyPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 20), 0);

        when(auditLogService.getAuditLog(
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAuditLog_WithAllFilters_CombinesFiltersCorrectly() throws Exception {
        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 23, 23, 59);

        Page<AuditLogResponse> auditPage = new PageImpl<>(
                List.of(auditLogResponse), PageRequest.of(0, 20), 1);

        when(auditLogService.getAuditLog(any(), any(), anyString(), anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(auditPage);

        mockMvc.perform(get("/admin/audit")
                        .param("from", "2026-06-01T00:00:00")
                        .param("to", "2026-06-23T23:59:59")
                        .param("action", "TASK_CREATED")
                        .param("userId", "2")
                        .param("entityType", "TASK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].auditId").value("audit-uuid-123"));
    }

    @Test
    void getAuditLog_MultipleAuditLogs_ReturnsAllContent() throws Exception {
        AuditLogResponse secondLog = AuditLogResponse.builder()
                .auditId("audit-uuid-456")
                .action("USER_REGISTERED")
                .entityType("USER")
                .entityId(5L)
                .performedBy(5L)
                .performedByName("system")
                .build();

        Page<AuditLogResponse> auditPage = new PageImpl<>(
                List.of(auditLogResponse, secondLog), PageRequest.of(0, 20), 2);

        when(auditLogService.getAuditLog(
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(auditPage);

        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].auditId").value("audit-uuid-123"))
                .andExpect(jsonPath("$.content[1].auditId").value("audit-uuid-456"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}