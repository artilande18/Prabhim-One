package com.example.registeration.dto;

import jakarta.validation.constraints.NotBlank;

public class TokenRefreshRequest {

    @NotBlank
    private String refresh;

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }
    
}
