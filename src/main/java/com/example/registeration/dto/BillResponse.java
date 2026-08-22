package com.example.registeration.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BillResponse {

    @JsonProperty("id")
    private String billNumber;

    @JsonProperty("db_id")
    private UUID dbId;

    @JsonProperty("original_po_id")
    private String originalPoId;

    private LocalDate date;

    @JsonProperty("due_date")
    private LocalDate dueDate;

    private String vendor;
    private String category;
    private String status;
    private BigDecimal amount;
    private BigDecimal tax;
    private BigDecimal total;
    private String notes;

    @JsonProperty("converted_at")
    private LocalDateTime convertedAt;

    public BillResponse() {
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public UUID getDbId() {
        return dbId;
    }

    public void setDbId(UUID dbId) {
        this.dbId = dbId;
    }

    public String getOriginalPoId() {
        return originalPoId;
    }

    public void setOriginalPoId(String originalPoId) {
        this.originalPoId = originalPoId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
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

    public LocalDateTime getConvertedAt() {
        return convertedAt;
    }

    public void setConvertedAt(LocalDateTime convertedAt) {
        this.convertedAt = convertedAt;
    }
}
