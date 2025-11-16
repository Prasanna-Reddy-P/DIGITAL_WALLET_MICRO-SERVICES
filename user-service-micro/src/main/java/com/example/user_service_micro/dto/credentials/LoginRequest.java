package com.example.user_service_micro.dto.credentials;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request containing user's email and password")
public class LoginRequest {

    @Schema(
            description = "User email address used for login",
            example = "john.doe@example.com",
            required = true
    )
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(
            description = "User's login password",
            example = "Password@123",
            required = true
    )
    @NotBlank(message = "Password is required")
    private String password;

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
