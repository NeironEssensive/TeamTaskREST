package com.example.team_task.dto.kafka;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {
    private String eventId;          
    private String eventType;        
    private Long taskId;
    private String title;
    private String status;           
    private String priority;         
    private Long userId;             
    private Long performedBy;        
    private String performedByName;  
    private Map<String, FieldChange> changes;   
    private LocalDateTime timestamp;
    private String ipAddress;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class FieldChange {
    private String oldValue;
    private String newValue;
}
