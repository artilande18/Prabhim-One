package com.example.registeration.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.registeration.dto.PaymentRequest;
import com.example.registeration.dto.PaymentResponse;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtService jwtService;

    public PaymentController(PaymentService paymentService, JwtService jwtService) {
        this.paymentService = paymentService;
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
    public ResponseEntity<?> listPayments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "invoice_id", required = false) UUID invoiceId,
            @RequestParam(name = "customer_id", required = false) UUID customerId) {
        UUID userId = getUserId(authHeader);
        List<PaymentResponse> list = paymentService.listPayments(userId, invoiceId, customerId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getPayment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {
        UUID userId = getUserId(authHeader);
        PaymentResponse response = paymentService.getPayment(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public ResponseEntity<?> recordPayment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody PaymentRequest request) {
        UUID userId = getUserId(authHeader);
        PaymentResponse response = paymentService.recordPayment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deletePayment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {
        UUID userId = getUserId(authHeader);
        paymentService.deletePayment(id, userId);
        return ResponseEntity.ok(Map.of(
                "message", "Payment record deleted successfully."
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
