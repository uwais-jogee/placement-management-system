package com.example.placementmanagementsystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * EmailToken model used to authenticate the email links.
 */
@Entity
public class EmailToken {

    @Id
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public EmailToken(int expiryMinutes) {
        token = UUID.randomUUID().toString(); // Generate unique token
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusMinutes(expiryMinutes); // Set expiry time
    }

    public EmailToken() {
        token = UUID.randomUUID().toString(); // Generate unique token
        createdAt = LocalDateTime.now();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}