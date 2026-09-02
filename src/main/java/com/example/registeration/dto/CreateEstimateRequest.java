package com.example.registeration.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateEstimateRequest {

    @NotNull
    private UUID customer;

    @NotNull
    @JsonProperty("estimate_date")
    private LocalDate estimateDate;

    @NotNull
    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @NotBlank
    private String currency;

    private String notes;

    @JsonProperty("terms_and_conditions")
    private String termsAndConditions;

    @NotEmpty
    private List<EstimateItemRequest> items;

    // Getters and Setters

    public UUID getCustomer() {
        return customer;
    }

    public void setCustomer(UUID customer) {
        this.customer = customer;
    }

    public LocalDate getEstimateDate() {
        return estimateDate;
    }

    public void setEstimateDate(LocalDate estimateDate) {
        this.estimateDate = estimateDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public List<EstimateItemRequest> getItems() {
        return items;
    }

    public void setItems(List<EstimateItemRequest> items) {
        this.items = items;
    }
}
