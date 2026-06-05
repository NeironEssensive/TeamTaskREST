package com.example.team_task.dto.kafka;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {
    private String eventId;
    private String eventType;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private Long performedBy;
    private String performedByName;
    private Map<String, String> changes;
    private LocalDateTime timestamp;
    private String idAddress;
}
