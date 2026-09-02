package com.example.registeration.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.registeration.dto.PurchaseRequest;
import com.example.registeration.dto.PurchaseResponse;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.PurchaseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final JwtService jwtService;

    public PurchaseController(PurchaseService purchaseService, JwtService jwtService) {
        this.purchaseService = purchaseService;
        this.jwtService = jwtService;
    }

    private UUID getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or does not start with Bearer");
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractUserId(token);
    }

    @PostMapping({"", "/"})
    public ResponseEntity<?> createPurchase(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody PurchaseRequest request) {

        UUID userId = getUserId(authHeader);
        PurchaseResponse created = purchaseService.createPurchase(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<?> listPurchases(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize) {

        getUserId(authHeader);
        Page<PurchaseResponse> recordsPage = purchaseService.listPurchases(search, status, page, pageSize);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", recordsPage.getTotalElements());
        response.put("results", recordsPage.getContent());

        return ResponseEntity.ok(response);
    }

    @GetMapping({"/{id}", "/{id}/"})
    public ResponseEntity<?> getPurchaseDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {

        getUserId(authHeader);
        PurchaseResponse record = purchaseService.getPurchaseDetails(id);
        return ResponseEntity.ok(record);
    }

    @RequestMapping(value = {"/{id}", "/{id}/"}, method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updatePurchase(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody PurchaseRequest request) {

        UUID userId = getUserId(authHeader);
        PurchaseResponse updated = purchaseService.updatePurchase(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping({"/{id}", "/{id}/"})
    public ResponseEntity<?> deletePurchase(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {

        UUID userId = getUserId(authHeader);
        PurchaseResponse cancelled = purchaseService.deletePurchase(id, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Purchase cancelled successfully.");
        response.put("cancelled_at", cancelled.getCreatedAt());

        return ResponseEntity.ok(response);
    }
}
