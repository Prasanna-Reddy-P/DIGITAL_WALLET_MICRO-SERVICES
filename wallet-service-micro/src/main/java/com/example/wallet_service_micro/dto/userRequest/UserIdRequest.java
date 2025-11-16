package com.example.wallet_service_micro.dto.userRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object carrying a user ID")
public class UserIdRequest {

    @Schema(description = "Unique ID of the user", example = "42")
    private Long userId;

    public UserIdRequest() {}

    public UserIdRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
