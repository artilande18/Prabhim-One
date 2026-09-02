package com.example.registeration.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateCreditNoteRequest {

    @NotNull
    @JsonAlias({"customer_id", "customerId"})
    private UUID customer;

    @JsonAlias({"invoice_id", "invoiceId"})
    private UUID invoice;

    @NotNull
    @JsonProperty("credit_note_date")
    @JsonAlias({"creditNoteDate", "date"})
    private LocalDate creditNoteDate;

    @NotBlank
    private String currency;

    private String reason;
    private String notes;

    @JsonProperty("terms_and_conditions")
    @JsonAlias({"termsAndConditions", "terms"})
    private String termsAndConditions;

    @NotEmpty
    private List<CreditNoteItemRequest> items;

    public CreateCreditNoteRequest() {
    }

    public UUID getCustomer() {
        return customer;
    }

    public void setCustomer(UUID customer) {
        this.customer = customer;
    }

    public UUID getInvoice() {
        return invoice;
    }

    public void setInvoice(UUID invoice) {
        this.invoice = invoice;
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

    public List<CreditNoteItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CreditNoteItemRequest> items) {
        this.items = items;
    }
}
