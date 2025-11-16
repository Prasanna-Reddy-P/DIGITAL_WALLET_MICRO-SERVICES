package com.example.user_service_micro.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing user information")
public class UserInfoResponse {

    @Schema(description = "Full name of the user", example = "Prasanna Kumar")
    private String name;

    @Schema(description = "Email address of the user", example = "prasanna@example.com")
    private String email;

    @Schema(description = "Role assigned to the user", example = "ADMIN")
    private String role;

    public UserInfoResponse() {}

    public UserInfoResponse(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
