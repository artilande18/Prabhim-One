package com.example.registeration.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateCreditNoteRequest {

    @JsonProperty("credit_note_date")
    @JsonAlias({"creditNoteDate", "date"})
    private LocalDate creditNoteDate;

    private String currency;
    private String status;
    private String reason;
    private String notes;

    @JsonProperty("terms_and_conditions")
    @JsonAlias({"termsAndConditions", "terms"})
    private String termsAndConditions;

    public UpdateCreditNoteRequest() {
    }

    public LocalDate getCreditNoteDate() {
        return creditNoteDate;
    }

    public void setCreditNoteDate(LocalDate creditNoteDate) {
        this.creditNoteDate = creditNoteDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
