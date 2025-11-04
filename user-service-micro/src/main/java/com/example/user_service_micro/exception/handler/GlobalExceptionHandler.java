package com.example.user_service_micro.exception.handler;

import com.example.user_service_micro.dto.risk.ErrorResponse;
import com.example.user_service_micro.exception.auth.ForbiddenException;
import com.example.user_service_micro.exception.auth.InvalidCredentialsException;
import com.example.user_service_micro.exception.auth.UnauthorizedException;
import com.example.user_service_micro.exception.user.UserAlreadyExistsException;
import com.example.user_service_micro.exception.user.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // --------------------------------------------------------------------
    // ✅ 1️⃣ Validation Errors (Bad Request)
    // --------------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_ERROR",
                "Validation failed for request body",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // --------------------------------------------------------------------
    // ✅ 2️⃣ Authentication / Authorization Errors
    // --------------------------------------------------------------------
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex,
                                                            HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex,
                                                         HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                  HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", ex.getMessage(), request);
    }

    // --------------------------------------------------------------------
    // ✅ 3️⃣ User-related Errors
    // --------------------------------------------------------------------
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex,
                                                            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex,
                                                                 HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "USER_ALREADY_EXISTS", ex.getMessage(), request);
    }

    // --------------------------------------------------------------------
    // ✅ 4️⃣ Catch-all for unhandled exceptions
    // --------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
                                                                HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Unexpected error: " + ex.getMessage(), request);
    }

    // --------------------------------------------------------------------
    // ✅ Utility method — standardized error builder
    // --------------------------------------------------------------------
    private ResponseEntity<ErrorResponse> buildError(HttpStatus status,
                                                     String errorCode,
                                                     String message,
                                                     HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
                request.getRequestURI(),
                null // fieldErrors (only for validation errors)
        );

        return ResponseEntity.status(status).body(response);
    }
}
