package com.example.user_service_micro.dto.risk;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standardized error response returned by the API")
public class ErrorResponse {

    @Schema(
            description = "The timestamp when the error occurred",
            example = "2025-01-30T14:22:11"
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "HTTP status code",
            example = "400"
    )
    private int status;

    @Schema(
            description = "Short description of the error",
            example = "Bad Request"
    )
    private String error;

    @Schema(
            description = "Application-specific error code",
            example = "USER_001"
    )
    private String errorCode;

    @Schema(
            description = "Detailed error message",
            example = "Email format is invalid"
    )
    private String message;

    @Schema(
            description = "The endpoint path where the error occurred",
            example = "/api/auth/signup"
    )
    private String path;

    @Schema(
            description = "Validation errors for specific fields (if applicable)",
            example = """
        {
            "email": "Invalid email format",
            "password": "Password must be at least 8 characters"
        }
        """
    )
    private Map<String, String> fieldErrors;

    // Constructors
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String errorCode, String message, String path, Map<String, String> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String errorCode, String message, Map<String, String> fieldErrors) {
        this(timestamp, status, error, errorCode, message, null, fieldErrors);
    }

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String errorCode, String message) {
        this(timestamp, status, error, errorCode, message, null, null);
    }

    public ErrorResponse(int status, String error, String errorCode, String message, String path) {
        this(LocalDateTime.now(), status, error, errorCode, message, path, null);
    }

    // Getters & Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }
}
