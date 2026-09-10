package com.fpl.stats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a player cannot be found in the database by its FPL element ID.
 *
 * <p>Mapped to HTTP 404 via {@link ResponseStatus}.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PlayerNotFoundException extends RuntimeException {

    /**
     * Constructs a {@code PlayerNotFoundException} for the given FPL element ID.
     *
     * @param fplId the FPL element ID that could not be found
     */
    public PlayerNotFoundException(int fplId) {
        super("Player not found with FPL ID: " + fplId);
    }
}
