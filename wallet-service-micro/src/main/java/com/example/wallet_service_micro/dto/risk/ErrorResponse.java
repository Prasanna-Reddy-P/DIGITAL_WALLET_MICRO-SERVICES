package com.example.wallet_service_micro.dto.risk;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response model returned for failed requests.")
public class ErrorResponse {

    @Schema(
            description = "The timestamp when the error occurred",
            example = "2025-02-14T10:35:21"
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "HTTP status code of the error",
            example = "400"
    )
    private int status;

    @Schema(
            description = "General description of the error",
            example = "Validation failed"
    )
    private String message;

    @Schema(
            description = "Field-specific error messages (appears only for validation errors)",
            example = "{\"amount\": \"Amount must be positive\"}"
    )
    private Map<String, String> errors;

    public ErrorResponse() {}

    public ErrorResponse(LocalDateTime timestamp, int status, String message, Map<String, String> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public ErrorResponse(LocalDateTime timestamp, int status, String message, String s, String s1) {
    }

    // Getters & Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, String> getErrors() { return errors; }
    public void setErrors(Map<String, String> errors) { this.errors = errors; }
}
