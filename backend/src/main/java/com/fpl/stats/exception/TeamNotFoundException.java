package com.fpl.stats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a team cannot be found in the database by its FPL team ID.
 *
 * <p>Mapped to HTTP 404 via {@link ResponseStatus}.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TeamNotFoundException extends RuntimeException {

    /**
     * Constructs a {@code TeamNotFoundException} for the given FPL team ID.
     *
     * @param fplTeamId the FPL team ID that could not be found
     */
    public TeamNotFoundException(long fplTeamId) {
        super("Team not found with FPL ID: " + fplTeamId);
    }
}
