package com.example.user_service_micro.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request object containing a user ID")
public class UserIdRequest {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be a positive number")
    @Schema(description = "Unique ID of the user", example = "101")
    private Long userId;

    public UserIdRequest() {}

    public UserIdRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
