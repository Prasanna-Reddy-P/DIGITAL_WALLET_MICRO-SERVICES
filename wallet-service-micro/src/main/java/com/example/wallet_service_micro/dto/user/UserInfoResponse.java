package com.example.wallet_service_micro.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing user profile information along with wallet balance")
public class UserInfoResponse {

    @Schema(description = "Full name of the user", example = "Prasanna Reddy")
    private String name;

    @Schema(description = "Email address of the user", example = "prasanna@example.com")
    private String email;

    @Schema(description = "Role assigned to the user", example = "USER")
    private String role;

    @Schema(description = "Total wallet balance for the user", example = "1500.75")
    private double balance;

    public UserInfoResponse(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public UserInfoResponse(String name, String email, String role, Double balance) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.balance = balance;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
