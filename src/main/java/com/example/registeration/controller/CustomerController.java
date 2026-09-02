package com.example.registeration.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.registeration.dto.CreateCustomerRequest;
import com.example.registeration.dto.UpdateCustomerRequest;
import com.example.registeration.dto.CustomerResponse;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final JwtService jwtService;

    public CustomerController(CustomerService customerService, JwtService jwtService) {
        this.customerService = customerService;
        this.jwtService = jwtService;
    }

    private UUID getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or does not start with Bearer");
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractUserId(token);
    }

    @PostMapping("/")
    public ResponseEntity<?> createCustomer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateCustomerRequest request) {

        UUID userId = getUserId(authHeader);
        CustomerResponse created = customerService.createCustomer(request, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Customer created successfully");
        response.put("data", created);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getCustomer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        UUID userId = getUserId(authHeader);
        CustomerResponse customer = customerService.getCustomer(id, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Customer details retrieved successfully");
        response.put("data", customer);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/")
    public ResponseEntity<?> updateCustomerFull(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id,
            @RequestBody UpdateCustomerRequest request) {

        UUID userId = getUserId(authHeader);
        CustomerResponse updated = customerService.updateCustomer(id, request, false, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Customer updated successfully");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/")
    public ResponseEntity<?> updateCustomerPartial(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id,
            @RequestBody UpdateCustomerRequest request) {

        UUID userId = getUserId(authHeader);
        CustomerResponse updated = customerService.updateCustomer(id, request, true, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Customer updated successfully");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteCustomer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        UUID userId = getUserId(authHeader);
        customerService.deleteCustomer(id, userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Customer deleted successfully",
                "data", Map.of()
        ));
    }

    @PostMapping("/{id}/restore/")
    public ResponseEntity<?> restoreCustomer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        UUID userId = getUserId(authHeader);
        CustomerResponse restored = customerService.restoreCustomer(id, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Customer restored successfully");
        response.put("data", restored);

        return ResponseEntity.ok(response);
    }
}
