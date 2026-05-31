package com.example.team_task.dto.auth;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshTokenData implements Serializable{
    private Long userId;
    private String username;
    private String password;
    private String role;
}
