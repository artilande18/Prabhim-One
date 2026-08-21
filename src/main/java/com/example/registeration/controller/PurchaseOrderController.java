package com.example.registeration.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.registeration.dto.BillResponse;
import com.example.registeration.dto.PurchaseOrderRequest;
import com.example.registeration.dto.PurchaseOrderResponse;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.PurchaseOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/v1/purchase-orders", "/api/v1/purchaseorders"})
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final JwtService jwtService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService, JwtService jwtService) {
        this.purchaseOrderService = purchaseOrderService;
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
    public ResponseEntity<?> createPurchaseOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody PurchaseOrderRequest request) {

        UUID userId = getUserId(authHeader);
        PurchaseOrderResponse created = purchaseOrderService.createPurchaseOrder(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<?> listPurchaseOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize) {

        getUserId(authHeader);
        Page<PurchaseOrderResponse> pageResult = purchaseOrderService.listPurchaseOrders(search, status, page, pageSize);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", pageResult.getTotalElements());
        response.put("results", pageResult.getContent());

        return ResponseEntity.ok(response);
    }

    @GetMapping({"/{id}", "/{id}/"})
    public ResponseEntity<?> getPurchaseOrderDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {

        getUserId(authHeader);
        PurchaseOrderResponse po = purchaseOrderService.getPurchaseOrderDetails(id);
        return ResponseEntity.ok(po);
    }

    @RequestMapping(value = {"/{id}", "/{id}/"}, method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updatePurchaseOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody PurchaseOrderRequest request) {

        UUID userId = getUserId(authHeader);
        PurchaseOrderResponse updated = purchaseOrderService.updatePurchaseOrder(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping({"/{id}", "/{id}/"})
    public ResponseEntity<?> cancelPurchaseOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {

        UUID userId = getUserId(authHeader);
        PurchaseOrderResponse cancelled = purchaseOrderService.cancelPurchaseOrder(id, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Purchase order cancelled.");
        response.put("cancelled_at", cancelled.getCancelledAt());

        return ResponseEntity.ok(response);
    }

    @PostMapping({"/{id}/restore", "/{id}/restore/"})
    public ResponseEntity<?> restoreCancelledPO(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {

        UUID userId = getUserId(authHeader);
        PurchaseOrderResponse restored = purchaseOrderService.restoreCancelledPO(id, userId);
        return ResponseEntity.ok(restored);
    }

    @DeleteMapping({"/{id}/permanent", "/{id}/permanent/"})
    public ResponseEntity<?> permanentlyDeletePO(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {

        UUID userId = getUserId(authHeader);
        purchaseOrderService.permanentlyDeletePO(id, userId);

        return ResponseEntity.ok(Map.of(
                "message", "Purchase order permanently deleted."
        ));
    }

    @PostMapping({"/{id}/convert-to-bill", "/{id}/convert-to-bill/"})
    public ResponseEntity<?> convertToBill(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {

        UUID userId = getUserId(authHeader);
        String billNumber = body.get("bill_number");
        if (billNumber == null || billNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Field 'bill_number' is required"
            ));
        }

        BillResponse billResponse = purchaseOrderService.convertToBill(id, billNumber, userId);
        return ResponseEntity.ok(billResponse);
    }
}
