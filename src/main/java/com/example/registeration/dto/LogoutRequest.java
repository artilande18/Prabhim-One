package com.example.registeration.dto;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {
    
    @NotBlank
    private String refresh;

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }
}
