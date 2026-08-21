package com.example.registeration.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseResponse {

    @JsonProperty("id")
    private String purchaseNumber;

    @JsonProperty("db_id")
    private UUID dbId;

    private LocalDate date;
    private String vendor;
    private String category;
    private String reference;

    @JsonProperty("payment_mode")
    private String paymentMode;

    private String status;
    private BigDecimal amount;

    @JsonProperty("gst_rate")
    private Integer gstRate;

    private BigDecimal tax;
    private BigDecimal total;
    private String notes;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public PurchaseResponse() {
    }

    public String getPurchaseNumber() {
        return purchaseNumber;
    }

    public void setPurchaseNumber(String purchaseNumber) {
        this.purchaseNumber = purchaseNumber;
    }

    public UUID getDbId() {
        return dbId;
    }

    public void setDbId(UUID dbId) {
        this.dbId = dbId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getGstRate() {
        return gstRate;
    }

    public void setGstRate(Integer gstRate) {
        this.gstRate = gstRate;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
