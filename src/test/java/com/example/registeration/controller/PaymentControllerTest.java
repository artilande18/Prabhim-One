package com.example.registeration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.registeration.dto.PaymentRequest;
import com.example.registeration.dto.PaymentResponse;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.PaymentService;

class PaymentControllerTest {

    private MockMvc mockMvc;
    private PaymentService paymentService;
    private JwtService jwtService;

    private UUID userId;
    private String token;

    @BeforeEach
    void setUp() {
        paymentService = org.mockito.Mockito.mock(PaymentService.class);
        jwtService = org.mockito.Mockito.mock(JwtService.class);

        PaymentController controller = new PaymentController(paymentService, jwtService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        userId = UUID.randomUUID();
        token = "valid-test-token";
    }

    @Test
    void listPayments_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/payments/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication credentials were not provided."));
    }

    @Test
    void listPayments_WithToken_ShouldReturnList() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        PaymentResponse res = new PaymentResponse();
        res.setId(UUID.randomUUID());
        res.setPaymentNumber("PAY-000001");
        res.setAmount(BigDecimal.valueOf(500.00));
        res.setPaymentMode("UPI");

        when(paymentService.listPayments(eq(userId), any(), any())).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/payments/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].payment_number").value("PAY-000001"))
                .andExpect(jsonPath("$[0].amount").value(500.00))
                .andExpect(jsonPath("$[0].payment_mode").value("UPI"));
    }

    @Test
    void recordPayment_WithValidPayload_ShouldReturnCreated() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        UUID invoiceId = UUID.randomUUID();
        PaymentResponse res = new PaymentResponse();
        res.setId(UUID.randomUUID());
        res.setPaymentNumber("PAY-000001");
        res.setInvoiceId(invoiceId);
        res.setAmount(BigDecimal.valueOf(500.00));

        when(paymentService.recordPayment(any(PaymentRequest.class), eq(userId))).thenReturn(res);

        String jsonPayload = "{"
                + "\"invoice_id\":\"" + invoiceId + "\","
                + "\"amount\":500.00,"
                + "\"payment_mode\":\"UPI\","
                + "\"payment_date\":\"2026-09-02\""
                + "}";

        mockMvc.perform(post("/api/v1/payments/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payment_number").value("PAY-000001"))
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    void deletePayment_WithValidToken_ShouldReturnOk() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(jwtService.extractUserId(token)).thenReturn(userId);
        doNothing().when(paymentService).deletePayment(paymentId, userId);

        mockMvc.perform(delete("/api/v1/payments/" + paymentId + "/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payment record deleted successfully."));
    }
}
