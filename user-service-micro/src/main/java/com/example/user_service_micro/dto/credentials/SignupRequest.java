package com.example.user_service_micro.dto.credentials;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for user signup")
public class SignupRequest {

    @Schema(
            description = "Full name of the user registering",
            example = "John Doe",
            required = true
    )
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(
            description = "Email address of the user",
            example = "john.doe@example.com",
            required = true
    )
    @Email(message = "Invalid email format")
    private String email;

    @Schema(
            description = "Password chosen by the user",
            example = "StrongPass@123",
            required = true
    )
    @NotBlank(message = "Password is required")
    private String password;

    @Schema(
            description = "Age of the user (must be 18+)",
            example = "22",
            required = true,
            minimum = "18"
    )
    @NotNull(message = "Age field is must")
    @Min(value = 18, message = "User must be at least 18 years old")
    private int age;

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
