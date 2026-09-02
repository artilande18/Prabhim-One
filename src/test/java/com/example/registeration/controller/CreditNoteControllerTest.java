package com.example.registeration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.registeration.dto.CreateCreditNoteRequest;
import com.example.registeration.dto.CreditNoteResponse;
import com.example.registeration.dto.UpdateCreditNoteRequest;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.CreditNoteService;

class CreditNoteControllerTest {

    private MockMvc mockMvc;
    private CreditNoteService creditNoteService;
    private JwtService jwtService;

    private UUID userId;
    private String token;

    @BeforeEach
    void setUp() {
        creditNoteService = org.mockito.Mockito.mock(CreditNoteService.class);
        jwtService = org.mockito.Mockito.mock(JwtService.class);

        CreditNoteController controller = new CreditNoteController(creditNoteService, jwtService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        userId = UUID.randomUUID();
        token = "valid-test-token";
    }

    @Test
    void getCreditNotes_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/credit-notes/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication credentials were not provided."));
    }

    @Test
    void getCreditNotes_WithAuth_ShouldReturnPage() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        CreditNoteResponse res = new CreditNoteResponse();
        res.setId(UUID.randomUUID());
        res.setCreditNoteNumber("CN-000001");
        res.setGrandTotal(BigDecimal.valueOf(500.00));

        when(creditNoteService.getCreditNotes(any(), any(), any(), any(), eq(1), eq(10)))
                .thenReturn(new PageImpl<>(List.of(res)));

        mockMvc.perform(get("/api/v1/credit-notes/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.results[0].credit_note_number").value("CN-000001"));
    }

    @Test
    void createCreditNote_WithValidPayload_ShouldReturnCreated() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        CreditNoteResponse res = new CreditNoteResponse();
        res.setId(UUID.randomUUID());
        res.setCreditNoteNumber("CN-000001");

        when(creditNoteService.createCreditNote(any(CreateCreditNoteRequest.class))).thenReturn(res);

        String jsonPayload = "{"
                + "\"customer\":\"" + UUID.randomUUID() + "\","
                + "\"credit_note_date\":\"2026-09-02\","
                + "\"currency\":\"INR\","
                + "\"items\":[{\"product\":\"" + UUID.randomUUID() + "\",\"quantity\":1,\"unit_price\":100.00}]"
                + "}";

        mockMvc.perform(post("/api/v1/credit-notes/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.credit_note_number").value("CN-000001"));
    }

    @Test
    void getCreditNote_ShouldReturnDetails() throws Exception {
        UUID id = UUID.randomUUID();
        when(jwtService.extractUserId(token)).thenReturn(userId);

        Map<String, Object> details = Map.of(
                "id", id,
                "credit_note_number", "CN-000001",
                "status", "draft"
        );
        when(creditNoteService.getCreditNoteDetails(id)).thenReturn(details);

        mockMvc.perform(get("/api/v1/credit-notes/" + id + "/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.credit_note_number").value("CN-000001"));
    }

    @Test
    void updateCreditNote_ShouldReturnUpdated() throws Exception {
        UUID id = UUID.randomUUID();
        when(jwtService.extractUserId(token)).thenReturn(userId);

        CreditNoteResponse res = new CreditNoteResponse();
        res.setId(id);
        res.setCreditNoteNumber("CN-000001");
        res.setStatus("closed");

        when(creditNoteService.updateCreditNote(eq(id), any(UpdateCreditNoteRequest.class))).thenReturn(res);

        String jsonPayload = "{\"status\":\"closed\"}";

        mockMvc.perform(patch("/api/v1/credit-notes/" + id + "/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("closed"));
    }

    @Test
    void deleteCreditNote_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(jwtService.extractUserId(token)).thenReturn(userId);
        doNothing().when(creditNoteService).deleteCreditNote(id);

        mockMvc.perform(delete("/api/v1/credit-notes/" + id + "/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Credit note deleted successfully"));
    }
}
