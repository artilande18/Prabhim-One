package com.example.registeration.exception;

import java.util.Map;

public class ProfileValidationException extends RuntimeException {
    private final Map<String, String> details;

    public ProfileValidationException(Map<String, String> details) {
        super("Validation failed");
        this.details = details;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
