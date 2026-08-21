package com.example.registeration.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.registeration.dto.VendorRequest;
import com.example.registeration.dto.VendorResponse;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.VendorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/vendors")
public class VendorController {

    private final VendorService vendorService;
    private final JwtService jwtService;

    public VendorController(VendorService vendorService, JwtService jwtService) {
        this.vendorService = vendorService;
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
    public ResponseEntity<?> createVendor(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody VendorRequest request) {

        UUID userId = getUserId(authHeader);
        VendorResponse created = vendorService.createVendor(request, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Vendor created successfully");
        response.put("data", created);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<?> getVendors(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        UUID userId = getUserId(authHeader);
        List<VendorResponse> vendors = vendorService.listVendors(userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Vendors retrieved successfully");
        response.put("data", vendors);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getVendor(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        UUID userId = getUserId(authHeader);
        VendorResponse vendor = vendorService.getVendor(id, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Vendor details retrieved successfully");
        response.put("data", vendor);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/")
    public ResponseEntity<?> updateVendor(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id,
            @RequestBody VendorRequest request) {

        UUID userId = getUserId(authHeader);
        VendorResponse updated = vendorService.updateVendor(id, request, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Vendor updated successfully");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteVendor(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        UUID userId = getUserId(authHeader);
        vendorService.deleteVendor(id, userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vendor deleted successfully",
                "data", Map.of()
        ));
    }
}
