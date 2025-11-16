package com.example.wallet_service_micro.dto.userRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for fetching paginated user transactions")
public class UserTransactionRequest {

    @Schema(description = "Unique ID of the user whose transactions are requested",
            example = "101")
    private Long userId;

    @Schema(description = "Page number (0-based index)", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Number of records per page", example = "10", defaultValue = "10")
    private int size = 10;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
