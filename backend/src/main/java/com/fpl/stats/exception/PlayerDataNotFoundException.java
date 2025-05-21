package com.fpl.stats.exception;

public class PlayerDataNotFoundException extends RuntimeException {
    public PlayerDataNotFoundException(String message) {
        super(message);
    }

    public PlayerDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}