package com.example.registeration.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.registeration.dto.BillRequest;
import com.example.registeration.dto.BillResponse;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.BillService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/bills")
public class BillController {

    private final BillService billService;
    private final JwtService jwtService;

    public BillController(BillService billService, JwtService jwtService) {
        this.billService = billService;
        this.jwtService = jwtService;
    }

    private UUID getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or does not start with Bearer");
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractUserId(token);
    }

    @GetMapping("/")
    public ResponseEntity<?> listBills(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize) {

        getUserId(authHeader);
        Page<BillResponse> billsPage = billService.listBills(page, pageSize);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", billsPage.getTotalElements());
        response.put("results", billsPage.getContent());

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value =  "/{id}/", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateBill(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @Valid @RequestBody BillRequest request) {

        UUID userId = getUserId(authHeader);
        BillResponse updated = billService.updateBill(id, request, userId);
        return ResponseEntity.ok(updated);
    }
}
