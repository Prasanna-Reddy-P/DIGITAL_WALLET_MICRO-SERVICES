package com.example.user_service_micro.dto.risk;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String errorCode;
    private String message;
    private String path;
    private Map<String, String> fieldErrors;

    // ✅ Full constructor (supports all fields)
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String errorCode, String message, String path, Map<String, String> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    // ✅ Constructor for validation errors
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String errorCode, String message, Map<String, String> fieldErrors) {
        this(timestamp, status, error, errorCode, message, null, fieldErrors);
    }

    // ✅ Constructor for general errors
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String errorCode, String message) {
        this(timestamp, status, error, errorCode, message, null, null);
    }

    // ✅ Constructor for path-based errors
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
