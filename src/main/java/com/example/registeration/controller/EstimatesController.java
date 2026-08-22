package com.example.registeration.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.registeration.dto.CreateEstimateRequest;
import com.example.registeration.dto.EstimateResponse;
import com.example.registeration.dto.UpdateEstimateRequest;
import com.example.registeration.service.EstimateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/estimates")
public class EstimatesController {

    private final EstimateService estimateService;

    public EstimatesController(EstimateService estimateService) {
        this.estimateService = estimateService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getEstimates(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,

            @RequestParam(name = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(name = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {

        Page<EstimateResponse> estimatesPage = estimateService.getEstimates(
                search,
                status,
                startDate,
                endDate,
                page,
                pageSize
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", estimatesPage.getTotalElements());
        data.put("next", estimatesPage.hasNext() ? "/api/v1/estimates/?page=" + (estimatesPage.getNumber() + 2) : null);
        data.put("previous", estimatesPage.hasPrevious() ? "/api/v1/estimates/?page=" + estimatesPage.getNumber() : null);
        data.put("page", estimatesPage.getNumber() + 1);
        data.put("total_pages", estimatesPage.getTotalPages());
        data.put("results", estimatesPage.getContent());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Estimates retrieved successfully");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public ResponseEntity<?> createEstimate(
            @Valid @RequestBody CreateEstimateRequest request) {

        EstimateResponse created = estimateService.createEstimate(request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Estimate created successfully");
        response.put("data", created);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getEstimate(
            @PathVariable UUID id) {

        Map<String, Object> details = estimateService.getEstimateDetails(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Estimate details retrieved successfully");
        response.put("data", details);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/")
    public ResponseEntity<?> updateEstimate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEstimateRequest request) {

        EstimateResponse updated = estimateService.updateEstimate(id, request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Estimate updated successfully");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteEstimate(
            @PathVariable UUID id) {

        estimateService.deleteEstimate(id);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Estimate deleted successfully",
                        "data", Map.of()
                )
        );
    }
}
