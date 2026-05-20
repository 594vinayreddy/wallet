package com.ewallet.wallet_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {

        String msg = ex.getMessage();

        // Wallet not found → 404
        if (msg != null && msg.startsWith("Wallet not found")) {
            log.warn("Not found: {}", msg);
            return build(HttpStatus.NOT_FOUND, msg);
        }

        // Wrong PIN → 400 Bad Request
        if (msg != null && (
                msg.equals("Invalid PIN.") ||
                        msg.equals("Current PIN is incorrect.")
        )) {
            log.warn("Bad request: {}", msg);
            return build(HttpStatus.BAD_REQUEST, msg);
        }

// No PIN set → 409 Conflict
        if (msg != null &&
                msg.equals("No PIN set for this wallet.")
        ) {
            log.warn("Conflict: {}", msg);
            return build(HttpStatus.CONFLICT, msg);
        }

        // Insufficient balance → 422
        if (msg != null && msg.equals("Insufficient balance.")) {
            log.warn("Unprocessable: {}", msg);
            return build(HttpStatus.UNPROCESSABLE_ENTITY, msg);
        }

        // PIN setup issues → 409 Conflict
        if (msg != null && (
                msg.startsWith("PIN already set") ||
                        msg.startsWith("No PIN set. Please set a PIN first.")
        )) {
            log.warn("Conflict: {}", msg);
            return build(HttpStatus.CONFLICT, msg);
        }

        // Everything else → 400
        log.error("Unhandled runtime exception: {}", msg, ex);
        return build(HttpStatus.BAD_REQUEST, msg != null ? msg : "An unexpected error occurred.");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "status",    status.value(),
                        "error",     status.getReasonPhrase(),
                        "message",   message,
                        "timestamp", LocalDateTime.now().toString()
                ));
    }
}