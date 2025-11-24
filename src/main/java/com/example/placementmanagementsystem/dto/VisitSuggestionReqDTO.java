package com.example.placementmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for Visit Suggestion Request, send by the front end to request visit suggestions for a given week
 */
public class VisitSuggestionReqDTO {

    private Long placementId;
    private String week;
    private int duration; // Meeting duration in minutes
    @JsonProperty("isOnline")
    private boolean isOnline;
    private int travelDuration; // Travel time in minutes

    public Long getPlacementId() {
        return placementId;
    }

    public void setPlacementId(Long placementId) {
        this.placementId = placementId;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public int getTravelDuration() {
        return travelDuration;
    }

    public void setTravelDuration(int travelDuration) {
        this.travelDuration = travelDuration;
    }
}