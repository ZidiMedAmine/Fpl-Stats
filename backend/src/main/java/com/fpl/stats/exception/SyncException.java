package com.fpl.stats.exception;

/**
 * Thrown when an error occurs during an FPL data synchronisation operation.
 */
public class SyncException extends RuntimeException {

    /**
     * Constructs a {@code SyncException} with the specified detail message.
     *
     * @param message a human-readable description of the sync failure
     */
    public SyncException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code SyncException} with the specified detail message and underlying cause.
     *
     * @param message a human-readable description of the sync failure
     * @param cause   the exception that triggered this sync failure
     */
    public SyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
