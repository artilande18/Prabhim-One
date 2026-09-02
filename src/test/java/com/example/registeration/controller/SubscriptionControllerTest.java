package com.example.registeration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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

import com.example.registeration.dto.BillingHistoryDTO;
import com.example.registeration.dto.CurrentSubscriptionResponse;
import com.example.registeration.dto.SubscribeResponse;
import com.example.registeration.dto.SubscriptionPlanDTO;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.SubscriptionService;

class SubscriptionControllerTest {

    private MockMvc mockMvc;
    private SubscriptionService subscriptionService;
    private JwtService jwtService;

    private UUID userId;
    private String token;

    @BeforeEach
    void setUp() {
        subscriptionService = org.mockito.Mockito.mock(SubscriptionService.class);
        jwtService = org.mockito.Mockito.mock(JwtService.class);

        SubscriptionController controller = new SubscriptionController(subscriptionService, jwtService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        userId = UUID.randomUUID();
        token = "valid-test-token";
    }

    @Test
    void getPlans_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/subscription/plans/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication credentials were not provided."));
    }

    @Test
    void getPlans_WithAuth_ShouldReturnPlans() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        SubscriptionPlanDTO plan = new SubscriptionPlanDTO(
                "plan_pro", "Professional", "Description",
                BigDecimal.valueOf(999.00), BigDecimal.valueOf(9990.00),
                "INR", "monthly", List.of("Feature 1"), -1, 5, true
        );
        when(subscriptionService.getPlans()).thenReturn(List.of(plan));

        mockMvc.perform(get("/api/v1/subscription/plans/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("plan_pro"))
                .andExpect(jsonPath("$[0].name").value("Professional"));
    }

    @Test
    void getCurrentSubscription_ShouldReturnActive() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        CurrentSubscriptionResponse current = new CurrentSubscriptionResponse();
        current.setPlanId("plan_pro");
        current.setPlanName("Professional");
        current.setStatus("active");

        when(subscriptionService.getCurrentSubscription(userId)).thenReturn(current);

        mockMvc.perform(get("/api/v1/subscription/current/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan_id").value("plan_pro"))
                .andExpect(jsonPath("$.status").value("active"));
    }

    @Test
    void subscribe_ShouldReturnOk() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        SubscribeResponse res = SubscribeResponse.orderInitiated("order_123456", BigDecimal.valueOf(999.00), "INR", "key_123", "plan_pro", "Professional");
        when(subscriptionService.subscribe(eq(userId), any())).thenReturn(res);

        String jsonPayload = "{\"plan_id\":\"plan_pro\",\"billing_cycle\":\"monthly\"}";

        mockMvc.perform(post("/api/v1/subscription/subscribe/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.order_id").value("order_123456"));
    }

    @Test
    void cancelSubscription_ShouldReturnOk() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        CurrentSubscriptionResponse cancelled = new CurrentSubscriptionResponse();
        cancelled.setPlanId("plan_pro");
        cancelled.setStatus("cancelled");

        when(subscriptionService.cancelSubscription(eq(userId), any())).thenReturn(cancelled);

        mockMvc.perform(post("/api/v1/subscription/cancel/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Too expensive\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Subscription cancelled successfully."));
    }

    @Test
    void getBillingHistory_ShouldReturnHistory() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        BillingHistoryDTO h = new BillingHistoryDTO();
        h.setInvoiceNumber("SUB-INV-000001");
        h.setPlanName("Professional");

        when(subscriptionService.getBillingHistory(userId)).thenReturn(List.of(h));

        mockMvc.perform(get("/api/v1/subscription/billing-history/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results[0].invoice_number").value("SUB-INV-000001"));
    }
}
