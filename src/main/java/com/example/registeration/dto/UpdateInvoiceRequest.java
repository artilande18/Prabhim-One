package com.example.registeration.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateInvoiceRequest {

    @JsonProperty("due_date")
    private LocalDate dueDate;
    
    private String notes;

    @JsonProperty("terms_and_conditions")
    private String termsAndConditions;

    // getters and setters

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
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
    
}
