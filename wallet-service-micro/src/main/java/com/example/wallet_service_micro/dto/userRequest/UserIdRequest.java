package com.example.wallet_service_micro.dto.userRequest;

public class UserIdRequest {
    private Long userId;

    public UserIdRequest() {}

    public UserIdRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

