package com.example.placementmanagementsystem.model;

import com.example.placementmanagementsystem.enumeration.VisitStatus;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Visit model class, used to store placement visit details.
 * Implements Comparable interface to compare two visit objects based on date and time
 */
@Entity
public class Visit implements Comparable<Visit> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "placement_id")
    private Placement placement;
    @Enumerated(EnumType.STRING)
    private VisitStatus status;
    private LocalDate date;
    private int meetingDuration; // Duration in minutes
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isOnline;
    @ManyToOne
    @JoinColumn(name = "address_place_id")
    private Address fromAddress;
    private int travelDuration; // Travel time in minutes

    @Override
    public int compareTo(@NotNull Visit otherVisit) {
        LocalDateTime thisDateTime = this.date.atTime(this.startTime);
        LocalDateTime otherDateTime = otherVisit.getDate().atTime(otherVisit.getStartTime());
        return thisDateTime.compareTo(otherDateTime);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Placement getPlacement() {
        return placement;
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getMeetingDuration() {
        return meetingDuration;
    }

    public void setMeetingDuration(int meetingDuration) {
        this.meetingDuration = meetingDuration;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public Address getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(Address fromAddress) {
        this.fromAddress = fromAddress;
    }

    public int getTravelDuration() {
        return travelDuration;
    }

    public void setTravelDuration(int travelDuration) {
        this.travelDuration = travelDuration;
    }
}