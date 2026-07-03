package com.example.starwars_api.exception;

public class SwapiUnavailableException extends RuntimeException {
    public SwapiUnavailableException(String message) {
        super(message);
    }
}
