package com.example.registeration.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import com.example.registeration.dto.InvoiceResponse;

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

import com.example.registeration.dto.CreateInvoiceRequest;
import com.example.registeration.dto.UpdateInvoiceRequest;
import com.example.registeration.service.InvoiceService;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getInvoices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,

            @RequestParam(name = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(name = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        
        Page<InvoiceResponse> invoicesPage = invoiceService.getInvoices(
                search,
                status,
                startDate,
                endDate,
                page,
                pageSize
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", invoicesPage.getTotalElements());
        data.put("next", invoicesPage.hasNext() ? "/api/invoices/?page=" + (invoicesPage.getNumber() + 2) : null);
        data.put("previous", invoicesPage.hasPrevious() ? "/api/invoices/?page=" + invoicesPage.getNumber() : null);
        data.put("page", invoicesPage.getNumber() + 1);
        data.put("total_pages", invoicesPage.getTotalPages());
        data.put("results", invoicesPage.getContent());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Invoices retrieved successfully");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public ResponseEntity<?> createInvoice(
            @RequestBody CreateInvoiceRequest request) {

        InvoiceResponse created = invoiceService.createInvoice(request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Invoice created successfully");
        response.put("data", created);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getInvoice(
            @PathVariable UUID id) {

        Map<String, Object> details = invoiceService.getInvoiceDetails(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Invoice details retrieved successfully");
        response.put("data", details);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/")
    public ResponseEntity<?> updateInvoice(
            @PathVariable UUID id,
            @RequestBody UpdateInvoiceRequest request) {

        InvoiceResponse updated = invoiceService.updateInvoice(id, request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Invoice updated successfully");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteInvoice(
            @PathVariable UUID id) {

        invoiceService.deleteInvoice(id);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Invoice deleted successfully",
                        "data", Map.of()
                )
        );
    }

    @PostMapping("/{id}/mark-sent/")
    public ResponseEntity<?> markSent(
            @PathVariable UUID id) {

        InvoiceResponse updated = invoiceService.markInvoiceSent(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Invoice marked as sent successfully");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }
}
