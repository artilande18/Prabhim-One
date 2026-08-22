package com.example.registeration.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.registeration.dto.ChangePasswordRequest;
import com.example.registeration.dto.ProfileResponse;
import com.example.registeration.dto.ProfileUpdateRequest;
import com.example.registeration.exception.PasswordChangeException;
import com.example.registeration.exception.ProfileValidationException;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.ProfileService;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final JwtService jwtService;

    public ProfileController(ProfileService profileService, JwtService jwtService) {
        this.profileService = profileService;
        this.jwtService = jwtService;
    }

    private UUID getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authentication credentials were not provided.");
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            return jwtService.extractUserId(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Authentication credentials were not provided.");
        }
    }

    @GetMapping({"", "/"})
    public ResponseEntity<?> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        UUID userId = getUserId(authHeader);
        ProfileResponse profile = profileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping({"", "/"})
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ProfileUpdateRequest request) {
        UUID userId = getUserId(authHeader);
        ProfileResponse updatedProfile = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully.",
                "profile", updatedProfile
        ));
    }

    @PostMapping({"/change-password", "/change-password/"})
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ChangePasswordRequest request) {
        UUID userId = getUserId(authHeader);
        profileService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of(
                "message", "Password changed successfully."
        ));
    }

    @PostMapping({"/upload-picture", "/upload-picture/"})
    public ResponseEntity<?> uploadPicture(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("profilePic") MultipartFile file) {
        UUID userId = getUserId(authHeader);
        String relativeUrl = profileService.uploadPicture(userId, file);
        
        
        String profilePicUrl = "http://localhost:9191" + relativeUrl;
        return ResponseEntity.ok(Map.of(
                "message", "Profile picture uploaded successfully.",
                "profilePicUrl", profilePicUrl
        ));
    }

    @DeleteMapping({"/picture", "/picture/"})
    public ResponseEntity<?> deletePicture(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        UUID userId = getUserId(authHeader);
        profileService.deletePicture(userId);
        return ResponseEntity.ok(Map.of(
                "message", "Profile picture removed."
        ));
    }

    // Public endpoint to download/render profile pictures
    @GetMapping("/picture/{userId}")
    public ResponseEntity<byte[]> getPicture(@PathVariable UUID userId) {
        Map<String, String> outContentType = new LinkedHashMap<>();
        byte[] imageBytes = profileService.getProfilePictureBytes(userId, outContentType);
        String contentType = outContentType.getOrDefault("contentType", "image/png");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(imageBytes);
    }

    // Controller specific Exception Handlers to match target JSON output structures
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(ProfileValidationException.class)
    public ResponseEntity<?> handleProfileValidation(ProfileValidationException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", ex.getMessage(),
                "details", ex.getDetails()
        ));
    }

    @ExceptionHandler(PasswordChangeException.class)
    public ResponseEntity<?> handlePasswordChange(PasswordChangeException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", ex.getMessage()
        ));
    }
}
