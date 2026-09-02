package com.example.registeration.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;
    private String ipAddress;
    private String deviceType;
    private String browser;
    private String os;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isCurrent;

    private String refreshToken;

    public UUID getId() {
    return id;
}

public void setId(UUID id) {
    this.id = id;
}

public UUID getUserId() {
    return userId;
}

public void setUserId(UUID userId) {
    this.userId = userId;
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

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }



    
}
