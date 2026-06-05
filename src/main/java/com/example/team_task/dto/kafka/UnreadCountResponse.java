package com.example.team_task.dto.kafka;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "count unreaded notifications")
public class UnreadCountResponse {

    @Schema(description = "count notifications", example = "3")
    private long unreadCount;
}
