package com.hyperproof.riskregister.api;

import com.hyperproof.riskregister.api.dto.ApiError;
import com.hyperproof.riskregister.exception.BadRequestException;
import com.hyperproof.riskregister.exception.BusinessRuleViolationException;
import com.hyperproof.riskregister.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "One or more fields are invalid",
                request,
                fields
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiError> handleBusinessRule(
            BusinessRuleViolationException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleBadRequest(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER",
                "Parameter '" + exception.getName() + "' has an invalid value",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "The request body contains malformed JSON or an unsupported enum value",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_VIOLATION",
                "The requested change conflicts with persisted data",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unhandled exception for {}", request.getRequestURI(), exception);
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                request,
                Map.of()
        );
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
