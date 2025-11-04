package com.example.user_service_micro.dto.credentials;

public class LoginResponse {
    private String message;
    private String token;
    private String name;
    private String email;
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
