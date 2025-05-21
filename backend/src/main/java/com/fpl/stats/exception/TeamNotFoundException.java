package com.fpl.stats.exception;


public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException(int teamId) {
        super("Team not found with ID: " + teamId);
    }
}