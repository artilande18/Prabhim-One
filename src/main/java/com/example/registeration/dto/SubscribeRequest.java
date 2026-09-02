package com.example.registeration.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class SubscribeRequest {

    @NotBlank(message = "Plan ID is required")
    @JsonProperty("plan_id")
    @JsonAlias({"planId", "plan"})
    private String planId;

    @JsonProperty("billing_cycle")
    @JsonAlias({"billingCycle", "cycle"})
    private String billingCycle = "monthly"; // monthly or yearly

    @JsonProperty("razorpay_order_id")
    @JsonAlias({"razorpayOrderId", "order_id", "orderId"})
    private String razorpayOrderId;

    @JsonProperty("razorpay_payment_id")
    @JsonAlias({"razorpayPaymentId", "payment_id", "paymentId"})
    private String razorpayPaymentId;

    @JsonProperty("razorpay_signature")
    @JsonAlias({"razorpaySignature", "signature"})
    private String razorpaySignature;

    public SubscribeRequest() {
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }
}
