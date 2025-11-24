package com.example.placementmanagementsystem.dto;

/**
 * Data Transfer Object for Visit Suggestion, used to provide suggestions for visit timings to the front end
 */
public class VisitSuggestionDTO {

    private int meetingDuration; // Meeting duration in minutes
    private boolean isOnline;
    private int travelDuration; // Travel duration in minutes
    private String date;
    private String meetingTime;
    private String travelStartTime;

    public VisitSuggestionDTO(int meetingDuration, boolean isOnline, int travelDuration, String date, String meetingTime, String travelStartTime) {
        this.meetingDuration = meetingDuration;
        this.isOnline = isOnline;
        this.travelDuration = travelDuration;
        this.date = date;
        this.meetingTime = meetingTime;
        this.travelStartTime = travelStartTime;
    }

    public int getMeetingDuration() {
        return meetingDuration;
    }

    public void setMeetingDuration(int meetingDuration) {
        this.meetingDuration = meetingDuration;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getTravelStartTime() {
        return travelStartTime;
    }

    public void setTravelStartTime(String travelStartTime) {
        this.travelStartTime = travelStartTime;
    }
}