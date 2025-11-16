package com.example.wallet_service_micro.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User details received from the User Service")
public class UserDTO {

    @Schema(description = "Unique ID of the user", example = "101")
    private Long id;

    @Schema(description = "Full name of the user", example = "Prasanna Reddy")
    private String name;

    @Schema(description = "Email address of the user", example = "prasanna@example.com")
    private String email;

    @Schema(description = "Role assigned to the user", example = "USER")
    private String role;

    public UserDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
