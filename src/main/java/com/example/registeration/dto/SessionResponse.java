package com.example.registeration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class SessionResponse {

    private UUID id;
    private String ipAddress;
    private String deviceType;
    private String browser;
    private String os;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isCurrent;

    // getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

}
