package com.example.wallet_service_micro.dto.user;

public class UserInfoResponse {
    private String name;
    private String email;
    private String role;
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
