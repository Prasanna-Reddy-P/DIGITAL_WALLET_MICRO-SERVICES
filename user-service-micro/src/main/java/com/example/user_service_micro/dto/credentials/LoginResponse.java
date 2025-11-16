package com.example.user_service_micro.dto.credentials;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after a successful login")
public class LoginResponse {

    @Schema(
            description = "Login status message",
            example = "Login successful!"
    )
    private String message;

    @Schema(
            description = "JWT authentication token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;

    @Schema(
            description = "Full name of the logged-in user",
            example = "John Doe"
    )
    private String name;

    @Schema(
            description = "Email of the logged-in user",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "Role assigned to the user",
            example = "USER"
    )
    private String role;

    public LoginResponse(String message, String token, String name, String email, String role) {
        this.message = message;
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
