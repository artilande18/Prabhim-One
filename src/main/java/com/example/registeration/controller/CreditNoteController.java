package com.example.registeration.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.registeration.dto.CreateCreditNoteRequest;
import com.example.registeration.dto.CreditNoteResponse;
import com.example.registeration.dto.UpdateCreditNoteRequest;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.CreditNoteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/credit-notes")
public class CreditNoteController {

    private final CreditNoteService creditNoteService;
    private final JwtService jwtService;

    public CreditNoteController(CreditNoteService creditNoteService, JwtService jwtService) {
        this.creditNoteService = creditNoteService;
        this.jwtService = jwtService;
    }

    private UUID getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authentication credentials were not provided.");
        }
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            return jwtService.extractUserId(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Authentication credentials were not provided.");
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getCreditNotes(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(name = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {

        getUserId(authHeader);

        Page<CreditNoteResponse> creditNotesPage = creditNoteService.getCreditNotes(
                search,
                status,
                startDate,
                endDate,
                page,
                pageSize
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", creditNotesPage.getTotalElements());
        data.put("next", creditNotesPage.hasNext() ? "/api/v1/credit-notes/?page=" + (creditNotesPage.getNumber() + 2) : null);
        data.put("previous", creditNotesPage.hasPrevious() ? "/api/v1/credit-notes/?page=" + creditNotesPage.getNumber() : null);
        data.put("page", creditNotesPage.getNumber() + 1);
        data.put("total_pages", creditNotesPage.getTotalPages());
        data.put("results", creditNotesPage.getContent());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Credit notes retrieved successfully");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public ResponseEntity<?> createCreditNote(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateCreditNoteRequest request) {

        getUserId(authHeader);

        CreditNoteResponse created = creditNoteService.createCreditNote(request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Credit note created successfully");
        response.put("data", created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getCreditNote(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        getUserId(authHeader);

        Map<String, Object> details = creditNoteService.getCreditNoteDetails(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Credit note details retrieved successfully");
        response.put("data", details);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/")
    public ResponseEntity<?> updateCreditNote(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id,
            @RequestBody UpdateCreditNoteRequest request) {

        getUserId(authHeader);

        CreditNoteResponse updated = creditNoteService.updateCreditNote(id, request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Credit note updated successfully");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteCreditNote(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        getUserId(authHeader);

        creditNoteService.deleteCreditNote(id);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Credit note deleted successfully",
                        "data", Map.of()
                )
        );
    }

    private static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", ex.getMessage()
        ));
    }
}
