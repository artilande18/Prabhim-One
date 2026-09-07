package com.example.registeration.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.registeration.dto.BillingHistoryDTO;
import com.example.registeration.dto.CancelSubscriptionRequest;
import com.example.registeration.dto.CurrentSubscriptionResponse;
import com.example.registeration.dto.SubscribeRequest;
import com.example.registeration.dto.SubscribeResponse;
import com.example.registeration.dto.SubscriptionPlanDTO;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.SubscriptionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final JwtService jwtService;

    public SubscriptionController(SubscriptionService subscriptionService, JwtService jwtService) {
        this.subscriptionService = subscriptionService;
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

    @GetMapping("/plans/")
    public ResponseEntity<?> getPlans(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        getUserId(authHeader);
        List<SubscriptionPlanDTO> plans = subscriptionService.getPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/current/")
    public ResponseEntity<?> getCurrentSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        UUID userId = getUserId(authHeader);
        CurrentSubscriptionResponse current = subscriptionService.getCurrentSubscription(userId);
        return ResponseEntity.ok(current);
    }

    @PostMapping("/subscribe/")
    public ResponseEntity<?> subscribe(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody SubscribeRequest request) {
        UUID userId = getUserId(authHeader);
        SubscribeResponse response = subscriptionService.subscribe(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel/")
    public ResponseEntity<?> cancelSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) CancelSubscriptionRequest request) {
        UUID userId = getUserId(authHeader);
        CurrentSubscriptionResponse response = subscriptionService.cancelSubscription(userId, request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Subscription cancelled successfully.",
                "subscription", response
        ));
    }

    @GetMapping("/billing-history/")
    public ResponseEntity<?> getBillingHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        UUID userId = getUserId(authHeader);
        List<BillingHistoryDTO> history = subscriptionService.getBillingHistory(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", history.size(),
                "results", history
        ));
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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", ex.getMessage()
        ));
    }
}
