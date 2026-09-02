package com.example.registeration.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.registeration.dto.CreateProformaInvoiceRequest;
import com.example.registeration.dto.ProformaInvoiceResponse;
import com.example.registeration.service.ProformaInvoiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/proforma-invoices")
public class ProformaInvoiceController {

    private final ProformaInvoiceService proformaInvoiceService;

    public ProformaInvoiceController(ProformaInvoiceService proformaInvoiceService) {
        this.proformaInvoiceService = proformaInvoiceService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getProformaInvoices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,

            @RequestParam(name = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(name = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {

        Page<ProformaInvoiceResponse> pfisPage = proformaInvoiceService.getProformaInvoices(
                search,
                status,
                startDate,
                endDate,
                page,
                pageSize
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", pfisPage.getTotalElements());
        data.put("next", pfisPage.hasNext() ? "/api/v1/proforma-invoices/?page=" + (pfisPage.getNumber() + 2) : null);
        data.put("previous", pfisPage.hasPrevious() ? "/api/v1/proforma-invoices/?page=" + pfisPage.getNumber() : null);
        data.put("page", pfisPage.getNumber() + 1);
        data.put("total_pages", pfisPage.getTotalPages());
        data.put("results", pfisPage.getContent());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Proforma Invoices retrieved successfully");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public ResponseEntity<?> createProformaInvoice(
            @Valid @RequestBody CreateProformaInvoiceRequest request) {

        ProformaInvoiceResponse created = proformaInvoiceService.createProformaInvoice(request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Proforma Invoice created successfully");
        response.put("data", created);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getProformaInvoice(
            @PathVariable UUID id) {

        Map<String, Object> details = proformaInvoiceService.getProformaInvoiceDetails(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Proforma Invoice details retrieved successfully");
        response.put("data", details);

        return ResponseEntity.ok(response);
    }
}
