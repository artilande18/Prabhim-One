package com.example.registeration.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.registeration.dto.*;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.ReportService;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final JwtService jwtService;

    public ReportController(ReportService reportService, JwtService jwtService) {
        this.reportService = reportService;
        this.jwtService = jwtService;
    }

    private UUID getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or does not start with Bearer");
        }
        String token = authHeader.replace("Bearer ", "").trim();
        return jwtService.extractUserId(token);
    }

    // 1. Profit & Loss Report
    @GetMapping({"/profit-loss/", "/profit-loss"})
    public ResponseEntity<?> getProfitLossReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        UUID userId = getUserId(authHeader);
        ProfitLossReportResponse report = reportService.getProfitLossReport(startDate, endDate, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Profit & Loss report generated successfully");
        response.put("data", report);

        return ResponseEntity.ok(response);
    }

    // 2. Sales Summary Report
    @GetMapping({"/sales-summary/", "/sales-summary"})
    public ResponseEntity<?> getSalesSummaryReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "period", required = false, defaultValue = "monthly") String period) {

        UUID userId = getUserId(authHeader);
        SalesSummaryReportResponse report = reportService.getSalesSummaryReport(startDate, endDate, period, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Sales summary report generated successfully");
        response.put("data", report);

        return ResponseEntity.ok(response);
    }

    // 3. GST / Tax Summary Report
    @GetMapping({"/tax-summary/", "/tax-summary"})
    public ResponseEntity<?> getTaxSummaryReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        UUID userId = getUserId(authHeader);
        TaxSummaryReportResponse report = reportService.getTaxSummaryReport(startDate, endDate, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "GST / Tax summary report generated successfully");
        response.put("data", report);

        return ResponseEntity.ok(response);
    }

    // 4. Customer Receivables Aging Report
    @GetMapping({"/receivables/", "/receivables"})
    public ResponseEntity<?> getReceivablesReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "as_of_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        UUID userId = getUserId(authHeader);
        ReceivablesReportResponse report = reportService.getReceivablesReport(asOfDate, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Customer receivables aging report generated successfully");
        response.put("data", report);

        return ResponseEntity.ok(response);
    }

    // 5. Vendor Payables Report
    @GetMapping({"/payables/", "/payables"})
    public ResponseEntity<?> getPayablesReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "as_of_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        UUID userId = getUserId(authHeader);
        PayablesReportResponse report = reportService.getPayablesReport(asOfDate, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Vendor payables report generated successfully");
        response.put("data", report);

        return ResponseEntity.ok(response);
    }
}
