package com.example.team_task.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request DTO for user registration")
public class RegisterRequest {
    
    @NotBlank(message = "name must not be null")
    @Size(min = 3, max = 21, message = "name must contains from 3 to 21 characters")
    @Schema(description = "Desired username", example = "john_doe", required = true, minLength = 3, maxLength = 21)
    private String name;
    
    @NotBlank(message = "email must not be null")
    @Email(message = "email must be valid")
    @Schema(description = "Email address", example = "john.doe@example.com", required = true, format = "email")
    private String email;
    
    @NotBlank(message = "password must not be null")
    @Size(min = 6, message = "password must contains at least 6 characters")
    @Schema(description = "Password (will be encrypted)", example = "securePass123", required = true, minLength = 6, format = "password")
    private String password;
}