package com.example.registeration.exception;

import java.util.Map;

public class SettingsValidationException extends RuntimeException {
    private final Map<String, String> details;

    public SettingsValidationException(Map<String, String> details) {
        super("Validation failed");
        this.details = details;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
