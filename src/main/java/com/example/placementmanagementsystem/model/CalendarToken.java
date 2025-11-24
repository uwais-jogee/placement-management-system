package com.example.placementmanagementsystem.model;

import java.time.LocalTime;

/**
 * CalendarToken model class used to store the Microsoft Calendar access token for a tutor, in the HTTP session.
 */
public class CalendarToken {

    private String tutorUsername;
    private String accessToken;
    private LocalTime expiresAt;

    public String getTutorUsername() {
        return tutorUsername;
    }

    public void setTutorUsername(String tutorUsername) {
        this.tutorUsername = tutorUsername;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LocalTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}