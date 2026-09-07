package com.example.registeration.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.registeration.dto.ExpenseRequest;
import com.example.registeration.dto.ExpenseResponse;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.ExpenseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final JwtService jwtService;

    public ExpenseController(ExpenseService expenseService, JwtService jwtService) {
        this.expenseService = expenseService;
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

    @GetMapping("/")
    public ResponseEntity<?> listExpenses(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        getUserId(authHeader);
        List<ExpenseResponse> list = expenseService.listExpenses();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/")
    public ResponseEntity<?> recordExpense(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ExpenseRequest request) {
        UUID userId = getUserId(authHeader);
        ExpenseResponse response = expenseService.createExpense(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteExpense(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {
        getUserId(authHeader);
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(Map.of(
                "message", "Expense deleted successfully."
        ));
    }

    // Controller specific Exception Handlers
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
}
