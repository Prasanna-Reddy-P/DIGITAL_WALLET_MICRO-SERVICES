package com.example.wallet_service_micro.dto.userRequest;


public class UserTransactionRequest {
    private Long userId;
    private int page = 0;
    private int size = 10;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
}

