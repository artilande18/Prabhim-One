package com.example.registeration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.registeration.dto.BillingHistoryDTO;
import com.example.registeration.dto.CancelSubscriptionRequest;
import com.example.registeration.dto.CurrentSubscriptionResponse;
import com.example.registeration.dto.SubscribeRequest;
import com.example.registeration.dto.SubscribeResponse;
import com.example.registeration.dto.SubscriptionPlanDTO;
import com.example.registeration.entity.SubscriptionBillingHistory;
import com.example.registeration.entity.UserSubscription;
import com.example.registeration.repository.SubscriptionBillingHistoryRepository;
import com.example.registeration.repository.UserRepository;
import com.example.registeration.repository.UserSubscriptionRepository;

class SubscriptionServiceTest {

    private UserSubscriptionRepository userSubscriptionRepository;
    private SubscriptionBillingHistoryRepository subscriptionBillingHistoryRepository;
    private UserRepository userRepository;

    private SubscriptionService subscriptionService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userSubscriptionRepository = mock(UserSubscriptionRepository.class);
        subscriptionBillingHistoryRepository = mock(SubscriptionBillingHistoryRepository.class);
        userRepository = mock(UserRepository.class);

        subscriptionService = new SubscriptionService(
                userSubscriptionRepository,
                subscriptionBillingHistoryRepository,
                userRepository
        );

        userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
    }

    @Test
    void getPlans_ShouldReturnAvailablePlans() {
        List<SubscriptionPlanDTO> plans = subscriptionService.getPlans();

        assertNotNull(plans);
        assertEquals(3, plans.size());
        assertEquals("plan_free", plans.get(0).getId());
        assertEquals("plan_pro", plans.get(1).getId());
        assertEquals("plan_enterprise", plans.get(2).getId());
    }

    @Test
    void getCurrentSubscription_WhenNoneExists_ShouldCreateDefaultFree() {
        when(userSubscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.empty());
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(i -> {
            UserSubscription s = i.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        CurrentSubscriptionResponse res = subscriptionService.getCurrentSubscription(userId);

        assertNotNull(res);
        assertEquals("plan_free", res.getPlanId());
        assertEquals("Free Starter", res.getPlanName());
        assertEquals("active", res.getStatus());
        verify(userSubscriptionRepository, times(1)).save(any(UserSubscription.class));
    }

    @Test
    void subscribe_WhenPaidPlanWithoutPaymentId_ShouldInitiateRazorpayOrder() {
        SubscribeRequest request = new SubscribeRequest();
        request.setPlanId("plan_pro");
        request.setBillingCycle("monthly");

        SubscribeResponse response = subscriptionService.subscribe(userId, request);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertNotNull(response.getOrderId());
        assertTrue(response.getOrderId().startsWith("order_"));
        assertEquals("plan_pro", response.getPlanId());
        assertEquals(0, BigDecimal.valueOf(999.00).compareTo(response.getAmount()));
    }

    @Test
    void subscribe_WhenPaidPlanWithPaymentId_ShouldActivateAndRecordBillingHistory() {
        SubscribeRequest request = new SubscribeRequest();
        request.setPlanId("plan_pro");
        request.setBillingCycle("monthly");
        request.setRazorpayOrderId("order_12345");
        request.setRazorpayPaymentId("pay_67890");

        when(userSubscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.empty());
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(i -> {
            UserSubscription s = i.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        when(subscriptionBillingHistoryRepository.count()).thenReturn(0L);

        SubscribeResponse response = subscriptionService.subscribe(userId, request);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertNotNull(response.getSubscription());
        assertEquals("plan_pro", response.getSubscription().getPlanId());
        assertEquals("active", response.getSubscription().getStatus());

        verify(subscriptionBillingHistoryRepository, times(1)).save(any(SubscriptionBillingHistory.class));
    }

    @Test
    void cancelSubscription_ShouldMarkCancelled() {
        UserSubscription sub = new UserSubscription();
        sub.setId(UUID.randomUUID());
        sub.setUserId(userId);
        sub.setPlanId("plan_pro");
        sub.setPlanName("Professional");
        sub.setStatus("active");
        sub.setEndDate(LocalDate.now().plusDays(20));

        when(userSubscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.of(sub));
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(i -> i.getArgument(0));

        CancelSubscriptionRequest req = new CancelSubscriptionRequest("Cost too high");
        CurrentSubscriptionResponse res = subscriptionService.cancelSubscription(userId, req);

        assertNotNull(res);
        assertEquals("cancelled", res.getStatus());
        assertFalse(res.getAutoRenew());
        assertNotNull(res.getCancelledAt());
    }

    @Test
    void getBillingHistory_ShouldReturnList() {
        SubscriptionBillingHistory h = new SubscriptionBillingHistory();
        h.setId(UUID.randomUUID());
        h.setInvoiceNumber("SUB-INV-000001");
        h.setPlanName("Professional");
        h.setAmount(BigDecimal.valueOf(999.00));
        h.setStatus("paid");

        when(subscriptionBillingHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(h));

        List<BillingHistoryDTO> history = subscriptionService.getBillingHistory(userId);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals("SUB-INV-000001", history.get(0).getInvoiceNumber());
    }
}
