package com.example.registeration.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.registeration.dto.LoginRequest;
import com.example.registeration.dto.LoginResponse;
import com.example.registeration.dto.LogoutRequest;
import com.example.registeration.dto.RegisterRequest;
import com.example.registeration.dto.ResendOtpRequest;
import com.example.registeration.dto.ResetPasswordRequest;
import com.example.registeration.dto.SessionResponse;
import com.example.registeration.dto.TokenRefreshRequest;
import com.example.registeration.dto.UserResponse;
import com.example.registeration.dto.VerifyEmailRequest;
import com.example.registeration.entity.Session;
import com.example.registeration.entity.User;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register/")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        UserResponse response = mapToUserResponse(user);
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "User registered successfully. A verification OTP has been sent to your email.",
                        "data", response
                )
        );
    }

    @PostMapping("/verify-email/")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        User user = authService.verifyEmail(request);
        UserResponse response = mapToUserResponse(user);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Email verified successfully. You can now log in.",
                        "data", response
                )
        );
    }

    @PostMapping("/resend-otp/")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request.getEmail(), request.getPurpose());

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Verification OTP sent to your email."
                )
        );
    }

    @PostMapping("/reset-password/")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Password reset successfully. You can now log in."
                )
        );
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setProfileImage(user.getProfileImage());
        response.setEmailVerified(user.isEmailVerified());
        response.setActive(user.isActive());
        response.setStaff(user.isStaff());
        response.setSuperuser(user.isSuperuser());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    @PostMapping("/login/")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged in successfully.",
                "data", response
        ));
    }

    @PostMapping("/token/refresh/")
    public ResponseEntity<?> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        Map<String, String> tokens = authService.refreshToken(request.getRefresh());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token refreshed successfully.",
                "data", tokens
        ));
    }

    @PostMapping("/logout/")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request,
            @RequestHeader("Authorization") String authHeader) {
        authService.logout(request.getRefresh());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully."
        ));
    }

    @GetMapping("/sessions/")
    public ResponseEntity<?> getSessions(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        UUID userId = jwtService.extractUserId(token);

        List<SessionResponse> sessions = authService.getSessions(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Active sessions retrieved successfully.",
                "data", sessions
        ));
    }


    @PostMapping("/logout-all/")
public ResponseEntity<?> logoutAll(@RequestHeader("Authorization") String authHeader) {
    try {
        String token = authHeader.replace("Bearer ", "");
        UUID userId = jwtService.extractUserId(token);

        System.out.println("Logout-All called for userId: " + userId);

        authService.logoutAll(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out from all active sessions successfully."
        ));
    } catch (IllegalArgumentException e) {
        // UUID parsing failed
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid token subject. Expected UUID but got something else."
        ));
    } catch (Exception e) {
        // Any other unexpected error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to log out from all sessions.",
                "error", e.getMessage()
        ));
    }
}


}
