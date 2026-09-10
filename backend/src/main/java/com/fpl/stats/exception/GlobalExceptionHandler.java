package com.fpl.stats.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler that maps domain exceptions to consistent JSON error responses.
 *
 * <p>All error responses share the same structure:
 * <pre>{@code
 * {
 *   "status": 404,
 *   "error": "Player not found with FPL ID: 42",
 *   "timestamp": "2026-09-07T10:15:30Z"
 * }
 * }</pre>
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link PlayerNotFoundException} and returns a 404 response.
     *
     * @param ex the exception carrying the not-found message
     * @return a 404 response with a structured error body
     */
    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePlayerNotFound(PlayerNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles {@link TeamNotFoundException} and returns a 404 response.
     *
     * @param ex the exception carrying the not-found message
     * @return a 404 response with a structured error body
     */
    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTeamNotFound(TeamNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles {@link ResponseStatusException} thrown by controllers for invalid input (e.g. bad position).
     *
     * @param ex the exception carrying the HTTP status and reason
     * @return a response with the exception's status and a structured error body
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        return errorResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    /**
     * Handles {@link SyncException} and returns a 500 response.
     *
     * @param ex the exception describing the sync failure
     * @return a 500 response with a structured error body
     */
    @ExceptionHandler(SyncException.class)
    public ResponseEntity<Map<String, Object>> handleSyncException(SyncException ex) {
        log.error("Sync operation failed: {}", ex.getMessage(), ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Sync operation failed: " + ex.getMessage());
    }

    /**
     * Builds a structured error response body.
     *
     * @param status  the HTTP status to return
     * @param message the error message to include in the body
     * @return a {@link ResponseEntity} with status and JSON-serialisable body
     */
    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "status", status.value(),
                "error", message != null ? message : status.getReasonPhrase(),
                "timestamp", Instant.now().toString()
        );
        return ResponseEntity.status(status).body(body);
    }
}
