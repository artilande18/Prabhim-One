package com.example.registeration.dto;

public class CancelSubscriptionRequest {

    private String reason;

    public CancelSubscriptionRequest() {
    }

    public CancelSubscriptionRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
