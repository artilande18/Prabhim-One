package com.example.registeration.dto;

public class SettingsSaveResponse {

    private String message;
    private String updatedAt;

    public SettingsSaveResponse() {
    }

    public SettingsSaveResponse(String message, String updatedAt) {
        this.message = message;
        this.updatedAt = updatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
