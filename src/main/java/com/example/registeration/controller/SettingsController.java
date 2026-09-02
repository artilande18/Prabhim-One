package com.example.registeration.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.registeration.dto.SettingsDTO;
import com.example.registeration.dto.SettingsSaveResponse;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.exception.SettingsValidationException;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.SettingsService;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final JwtService jwtService;

    public SettingsController(SettingsService settingsService, JwtService jwtService) {
        this.settingsService = settingsService;
        this.jwtService = jwtService;
    }

    private UUID getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authentication credentials were not provided.");
        }
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            return jwtService.extractUserId(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Authentication credentials were not provided.");
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getSettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        UUID userId = getUserId(authHeader);
        SettingsDTO settings = settingsService.getSettings(userId);
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateSettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SettingsDTO request) {
        UUID userId = getUserId(authHeader);
        SettingsSaveResponse response = settingsService.updateSettings(userId, request);
        return ResponseEntity.ok(response);
    }

    private static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(SettingsValidationException.class)
    public ResponseEntity<?> handleSettingsValidation(SettingsValidationException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", ex.getMessage(),
                "details", ex.getDetails()
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", ex.getMessage()
        ));
    }
}
