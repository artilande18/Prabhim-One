package com.example.registeration.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BillRequest {

    private String status;

    @JsonProperty("due_date")
    private LocalDate dueDate;

    private String notes;

    public BillRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
}
