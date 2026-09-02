package com.example.registeration.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.registeration.dto.BillingHistoryDTO;
import com.example.registeration.dto.CancelSubscriptionRequest;
import com.example.registeration.dto.CurrentSubscriptionResponse;
import com.example.registeration.dto.SubscribeRequest;
import com.example.registeration.dto.SubscribeResponse;
import com.example.registeration.dto.SubscriptionPlanDTO;
import com.example.registeration.entity.SubscriptionBillingHistory;
import com.example.registeration.entity.UserSubscription;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.SubscriptionBillingHistoryRepository;
import com.example.registeration.repository.UserRepository;
import com.example.registeration.repository.UserSubscriptionRepository;

@Service
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionBillingHistoryRepository subscriptionBillingHistoryRepository;
    private final UserRepository userRepository;

    public SubscriptionService(
            UserSubscriptionRepository userSubscriptionRepository,
            SubscriptionBillingHistoryRepository subscriptionBillingHistoryRepository,
            UserRepository userRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionBillingHistoryRepository = subscriptionBillingHistoryRepository;
        this.userRepository = userRepository;
    }

    private void validateUserExists(UUID userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    public List<SubscriptionPlanDTO> getPlans() {
        List<SubscriptionPlanDTO> plans = new ArrayList<>();

        plans.add(new SubscriptionPlanDTO(
                "plan_free",
                "Free Starter",
                "Essential invoicing and billing tools for freelancers and small businesses.",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "INR",
                "lifetime",
                List.of(
                        "Up to 5 Invoices/month",
                        "Basic Estimates & Credit Notes",
                        "Single User",
                        "Email Support",
                        "Standard PDF Templates"
                ),
                5,
                1,
                false
        ));

        plans.add(new SubscriptionPlanDTO(
                "plan_pro",
                "Professional",
                "Advanced tools and automations for growing companies and teams.",
                BigDecimal.valueOf(999.00),
                BigDecimal.valueOf(9990.00),
                "INR",
                "monthly",
                List.of(
                        "Unlimited Invoices & Estimates",
                        "Custom Branding & Company Logo",
                        "Up to 5 Team Members",
                        "Priority Email & Chat Support",
                        "Payment Gateway Integration (Razorpay)",
                        "Inventory & Product Tracking",
                        "Detailed Financial Reports"
                ),
                -1,
                5,
                true
        ));

        plans.add(new SubscriptionPlanDTO(
                "plan_enterprise",
                "Enterprise",
                "Full-featured suite for established businesses, warehouses, and multi-branches.",
                BigDecimal.valueOf(2499.00),
                BigDecimal.valueOf(24990.00),
                "INR",
                "monthly",
                List.of(
                        "Everything in Professional",
                        "Unlimited Team Members",
                        "Multi-Branch & Warehouse Management",
                        "Dedicated Account Manager",
                        "24/7 Phone & Email Priority Support",
                        "Custom Integrations & REST API Access",
                        "Automated Recurring Billing"
                ),
                -1,
                -1,
                false
        ));

        return plans;
    }

    public SubscriptionPlanDTO findPlanById(String planId) {
        return getPlans().stream()
                .filter(p -> p.getId().equalsIgnoreCase(planId) || p.getName().equalsIgnoreCase(planId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid subscription plan ID: " + planId));
    }

    @Transactional
    public CurrentSubscriptionResponse getCurrentSubscription(UUID userId) {
        validateUserExists(userId);

        UserSubscription subscription = userSubscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseGet(() -> createDefaultFreeSubscription(userId));

        return mapToCurrentSubscription(subscription);
    }

    @Transactional
    public SubscribeResponse subscribe(UUID userId, SubscribeRequest request) {
        validateUserExists(userId);

        if (request.getPlanId() == null || request.getPlanId().trim().isEmpty()) {
            throw new IllegalArgumentException("Plan ID is required");
        }

        SubscriptionPlanDTO plan = findPlanById(request.getPlanId().trim());
        String cycle = "yearly".equalsIgnoreCase(request.getBillingCycle()) ? "yearly" : "monthly";
        BigDecimal price = "yearly".equalsIgnoreCase(cycle) ? plan.getYearlyPrice() : plan.getMonthlyPrice();

        // If paid plan and no payment ID provided yet, initiate Razorpay order
        if (!"plan_free".equalsIgnoreCase(plan.getId()) && (request.getRazorpayPaymentId() == null || request.getRazorpayPaymentId().trim().isEmpty())) {
            String orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            String keyId = "rzp_test_57174562b3fc2c";
            return SubscribeResponse.orderInitiated(orderId, price, "INR", keyId, plan.getId(), plan.getName());
        }

        // Activate or upgrade subscription
        UserSubscription subscription = userSubscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseGet(() -> new UserSubscription());

        subscription.setUserId(userId);
        subscription.setPlanId(plan.getId());
        subscription.setPlanName(plan.getName());
        subscription.setStatus("active");
        subscription.setBillingCycle("plan_free".equalsIgnoreCase(plan.getId()) ? "lifetime" : cycle);
        subscription.setPrice(price);
        subscription.setCurrency("INR");
        subscription.setStartDate(LocalDate.now());

        if ("plan_free".equalsIgnoreCase(plan.getId())) {
            subscription.setEndDate(LocalDate.now().plusYears(100));
            subscription.setRenewalDate(null);
            subscription.setAutoRenew(false);
        } else if ("yearly".equalsIgnoreCase(cycle)) {
            subscription.setEndDate(LocalDate.now().plusYears(1));
            subscription.setRenewalDate(LocalDate.now().plusYears(1));
            subscription.setAutoRenew(true);
        } else {
            subscription.setEndDate(LocalDate.now().plusMonths(1));
            subscription.setRenewalDate(LocalDate.now().plusMonths(1));
            subscription.setAutoRenew(true);
        }

        subscription.setRazorpayOrderId(request.getRazorpayOrderId());
        subscription.setRazorpayPaymentId(request.getRazorpayPaymentId());
        subscription.setCancelledAt(null);
        subscription.setCancellationReason(null);
        if (subscription.getCreatedAt() == null) {
            subscription.setCreatedAt(LocalDateTime.now());
        }
        subscription.setUpdatedAt(LocalDateTime.now());

        UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);

        // Record Billing History
        if (price.compareTo(BigDecimal.ZERO) > 0 || !"plan_free".equalsIgnoreCase(plan.getId())) {
            long count = subscriptionBillingHistoryRepository.count();
            String invoiceNumber = String.format("SUB-INV-%06d", count + 1);

            SubscriptionBillingHistory history = new SubscriptionBillingHistory();
            history.setUserId(userId);
            history.setSubscriptionId(savedSubscription.getId());
            history.setInvoiceNumber(invoiceNumber);
            history.setPlanId(plan.getId());
            history.setPlanName(plan.getName());
            history.setAmount(price);
            history.setCurrency("INR");
            history.setBillingDate(LocalDate.now());
            history.setPaymentMethod("Razorpay");
            history.setPaymentId(request.getRazorpayPaymentId() != null ? request.getRazorpayPaymentId() : "pay_simulated");
            history.setOrderId(request.getRazorpayOrderId());
            history.setStatus("paid");
            history.setReceiptUrl("");
            history.setCreatedAt(LocalDateTime.now());
            history.setUpdatedAt(LocalDateTime.now());
            subscriptionBillingHistoryRepository.save(history);
        }

        CurrentSubscriptionResponse subResponse = mapToCurrentSubscription(savedSubscription);
        return SubscribeResponse.activated("Subscribed successfully.", subResponse);
    }

    @Transactional
    public CurrentSubscriptionResponse cancelSubscription(UUID userId, CancelSubscriptionRequest request) {
        validateUserExists(userId);

        UserSubscription subscription = userSubscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found for user"));

        if ("plan_free".equalsIgnoreCase(subscription.getPlanId())) {
            throw new IllegalArgumentException("Cannot cancel a free starter plan.");
        }

        subscription.setStatus("cancelled");
        subscription.setAutoRenew(false);
        subscription.setCancelledAt(LocalDateTime.now());
        if (request != null && request.getReason() != null) {
            subscription.setCancellationReason(request.getReason());
        }
        subscription.setUpdatedAt(LocalDateTime.now());

        UserSubscription saved = userSubscriptionRepository.save(subscription);
        return mapToCurrentSubscription(saved);
    }

    @Transactional(readOnly = true)
    public List<BillingHistoryDTO> getBillingHistory(UUID userId) {
        validateUserExists(userId);

        List<SubscriptionBillingHistory> list = subscriptionBillingHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return list.stream().map(h -> {
            BillingHistoryDTO dto = new BillingHistoryDTO();
            dto.setId(h.getId());
            dto.setInvoiceNumber(h.getInvoiceNumber());
            dto.setPlanId(h.getPlanId());
            dto.setPlanName(h.getPlanName());
            dto.setAmount(h.getAmount());
            dto.setCurrency(h.getCurrency());
            dto.setBillingDate(h.getBillingDate());
            dto.setPaymentMethod(h.getPaymentMethod());
            dto.setPaymentId(h.getPaymentId());
            dto.setOrderId(h.getOrderId());
            dto.setStatus(h.getStatus());
            dto.setReceiptUrl(h.getReceiptUrl());
            dto.setCreatedAt(h.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    private UserSubscription createDefaultFreeSubscription(UUID userId) {
        UserSubscription s = new UserSubscription();
        s.setUserId(userId);
        s.setPlanId("plan_free");
        s.setPlanName("Free Starter");
        s.setStatus("active");
        s.setBillingCycle("lifetime");
        s.setPrice(BigDecimal.ZERO);
        s.setCurrency("INR");
        s.setStartDate(LocalDate.now());
        s.setEndDate(LocalDate.now().plusYears(100));
        s.setRenewalDate(null);
        s.setAutoRenew(false);
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        return userSubscriptionRepository.save(s);
    }

    private CurrentSubscriptionResponse mapToCurrentSubscription(UserSubscription sub) {
        CurrentSubscriptionResponse res = new CurrentSubscriptionResponse();
        res.setId(sub.getId());
        res.setPlanId(sub.getPlanId());
        res.setPlanName(sub.getPlanName());
        res.setStatus(sub.getStatus());
        res.setBillingCycle(sub.getBillingCycle());
        res.setPrice(sub.getPrice());
        res.setCurrency(sub.getCurrency());
        res.setStartDate(sub.getStartDate());
        res.setEndDate(sub.getEndDate());
        res.setRenewalDate(sub.getRenewalDate());
        res.setAutoRenew(sub.getAutoRenew());
        res.setCancelledAt(sub.getCancelledAt());

        if (sub.getEndDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), sub.getEndDate());
            res.setDaysRemaining(days > 0 ? days : 0L);
        }

        try {
            SubscriptionPlanDTO plan = findPlanById(sub.getPlanId());
            res.setFeatures(plan.getFeatures());
            res.setMaxInvoices(plan.getMaxInvoices());
            res.setMaxUsers(plan.getMaxUsers());
        } catch (Exception e) {
            res.setFeatures(List.of("Invoicing & Billing Tools"));
            res.setMaxInvoices(5);
            res.setMaxUsers(1);
        }

        return res;
    }
}
