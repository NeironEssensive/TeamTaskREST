package com.example.team_task.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest{
 @NotBlank(message = "name must not be null")
 @Size(min = 3, max = 21, message = "name must contains from 3 to 21 characters")
 private String name;
 @NotBlank(message = "email must not be null")
 @Email(message = "email must be valid")
 private String email;
 @NotBlank(message = "password must not be null")
 @Size(min = 6, message = "password must contains at least 6 characters")
 private String password;
}