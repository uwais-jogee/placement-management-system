package com.example.placementmanagementsystem.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Data Transfer Object for Visit, used to transfer new visit data from front end to back end
 */
public class VisitDTO {

    private Long placementId;
    private int meetingDuration;
    private String meetingType;
    private LocalDate date;
    private LocalTime time;
    private int travelDuration;
    private String fromPlaceId;
    private String fromFormattedAddress;

    public Long getPlacementId() {
        return placementId;
    }

    public void setPlacementId(Long placementId) {
        this.placementId = placementId;
    }

    public int getMeetingDuration() {
        return meetingDuration;
    }

    public void setMeetingDuration(int meetingDuration) {
        this.meetingDuration = meetingDuration;
    }

    public String getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public int getTravelDuration() {
        return travelDuration;
    }

    public void setTravelDuration(int travelDuration) {
        this.travelDuration = travelDuration;
    }

    public String getFromPlaceId() {
        return fromPlaceId;
    }

    public void setFromPlaceId(String fromPlaceId) {
        this.fromPlaceId = fromPlaceId;
    }

    public String getFromFormattedAddress() {
        return fromFormattedAddress;
    }

    public void setFromFormattedAddress(String fromFormattedAddress) {
        this.fromFormattedAddress = fromFormattedAddress;
    }
}