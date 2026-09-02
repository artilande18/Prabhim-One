package com.example.registeration.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscribeResponse {

    private Boolean success;
    private String message;

    // Razorpay order details (for frontend checkout initiation)
    @JsonProperty("order_id")
    private String orderId;

    private BigDecimal amount;
    private String currency;

    @JsonProperty("key_id")
    private String keyId;

    @JsonProperty("plan_id")
    private String planId;

    @JsonProperty("plan_name")
    private String planName;

    // Active subscription details (when activated)
    private CurrentSubscriptionResponse subscription;

    public SubscribeResponse() {
    }

    public static SubscribeResponse orderInitiated(String orderId, BigDecimal amount, String currency, String keyId, String planId, String planName) {
        SubscribeResponse res = new SubscribeResponse();
        res.setSuccess(true);
        res.setMessage("Payment order created successfully.");
        res.setOrderId(orderId);
        res.setAmount(amount);
        res.setCurrency(currency);
        res.setKeyId(keyId);
        res.setPlanId(planId);
        res.setPlanName(planName);
        return res;
    }

    public static SubscribeResponse activated(String message, CurrentSubscriptionResponse subscription) {
        SubscribeResponse res = new SubscribeResponse();
        res.setSuccess(true);
        res.setMessage(message);
        res.setSubscription(subscription);
        return res;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public CurrentSubscriptionResponse getSubscription() {
        return subscription;
    }

    public void setSubscription(CurrentSubscriptionResponse subscription) {
        this.subscription = subscription;
    }
}
