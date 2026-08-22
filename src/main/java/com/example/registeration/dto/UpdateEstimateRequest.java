package com.example.registeration.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateEstimateRequest {

    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    private String notes;

    @JsonProperty("terms_and_conditions")
    private String termsAndConditions;

    private String status;

    // Getters and Setters

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
