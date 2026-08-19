package com.example.registeration.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.registeration.dto.CreateProductRequest;
import com.example.registeration.dto.UpdateProductRequest;
import com.example.registeration.dto.ProductListResponse;
import com.example.registeration.dto.ProductDetailsResponse;
import com.example.registeration.dto.BulkActionRequest;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final JwtService jwtService;

    public ProductController(ProductService productService, JwtService jwtService) {
        this.productService = productService;
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
    public ResponseEntity<?> createProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateProductRequest request) {

        UUID userId = getUserId(authHeader);
        ProductDetailsResponse created = productService.createProduct(request, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Product created successfully");
        response.put("data", created);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<?> getProducts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(name = "product_type", required = false) String productType,
            @RequestParam(required = false) UUID category,
            @RequestParam(required = false) UUID brand,
            @RequestParam(required = false) String status,
            @RequestParam(name = "track_inventory", required = false) Boolean trackInventory,
            @RequestParam(name = "stock_availability", required = false) String stockAvailability,
            @RequestParam(name = "start_date", required = false) String startDate,
            @RequestParam(name = "end_date", required = false) String endDate,
            @RequestParam(required = false) String ordering,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {

        UUID userId = getUserId(authHeader);
        Page<ProductListResponse> productsPage = productService.listProducts(
                search, productType, category, brand, status, trackInventory, stockAvailability, startDate, endDate, ordering, page, pageSize, userId
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", productsPage.getTotalElements());
        data.put("next", productsPage.hasNext() ? "/api/products/?page=" + (productsPage.getNumber() + 2) : null);
        data.put("previous", productsPage.hasPrevious() ? "/api/products/?page=" + productsPage.getNumber() : null);
        data.put("page", productsPage.getNumber() + 1);
        data.put("total_pages", productsPage.getTotalPages());
        data.put("results", productsPage.getContent());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Products retrieved successfully");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        UUID userId = getUserId(authHeader);
        ProductDetailsResponse product = productService.getProductDetails(id, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Product details retrieved successfully");
        response.put("data", product);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/")
    public ResponseEntity<?> updateProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id,
            @RequestBody UpdateProductRequest request) {

        UUID userId = getUserId(authHeader);
        ProductDetailsResponse updated = productService.updateProduct(id, request, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Product updated successfully");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        UUID userId = getUserId(authHeader);
        productService.deleteProduct(id, userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Product deleted successfully",
                "data", Map.of()
        ));
    }

    @PostMapping("/{id}/restore/")
    public ResponseEntity<?> restoreProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        UUID userId = getUserId(authHeader);
        ProductDetailsResponse restored = productService.restoreProduct(id, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Product restored successfully");
        response.put("data", restored);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/activate/")
    public ResponseEntity<?> activateProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        UUID userId = getUserId(authHeader);
        ProductDetailsResponse activated = productService.toggleProductStatus(id, true, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Product activated successfully");
        response.put("data", activated);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate/")
    public ResponseEntity<?> deactivateProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        UUID userId = getUserId(authHeader);
        ProductDetailsResponse deactivated = productService.toggleProductStatus(id, false, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Product deactivated successfully");
        response.put("data", deactivated);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-delete/")
    public ResponseEntity<?> bulkDeleteProducts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody BulkActionRequest request) {

        UUID userId = getUserId(authHeader);
        int count = productService.bulkDeleteProducts(request.getIds(), userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", count);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Bulk delete completed. " + count + " products soft-deleted successfully.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-activate/")
    public ResponseEntity<?> bulkActivateProducts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody BulkActionRequest request) {

        UUID userId = getUserId(authHeader);
        int count = productService.bulkToggleProductStatus(request.getIds(), true, userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", count);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Bulk activation completed. " + count + " products activated successfully.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-deactivate/")
    public ResponseEntity<?> bulkDeactivateProducts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody BulkActionRequest request) {

        UUID userId = getUserId(authHeader);
        int count = productService.bulkToggleProductStatus(request.getIds(), false, userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", count);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Bulk deactivation completed. " + count + " products deactivated successfully.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
