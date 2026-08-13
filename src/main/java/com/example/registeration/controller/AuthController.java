package com.example.registeration.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.registeration.dto.RegisterRequest;
import com.example.registeration.dto.ResendOtpRequest;
import com.example.registeration.dto.UserResponse;
import com.example.registeration.dto.VerifyEmailRequest;
import com.example.registeration.entity.User;
import com.example.registeration.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        UserResponse response = UserResponse.from(user);

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("success", true);

        result.put(
                "message",
                "User registered successfully. A verification OTP has been sent to your email."
        );

        result.put("data", response);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-email/")
    public ResponseEntity<?> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        User user = authService.verifyEmail(request);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message",
                        "Email verified successfully. You can now log in.",
                        "data", UserResponse.from(user)
                )
        );
    }

    @PostMapping("/resend-otp/")
    public ResponseEntity<?> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {

        authService.resendOtp(
                request.getEmail(),
                request.getPurpose()
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Verification OTP sent to your email."
                )
        );
    }

}
