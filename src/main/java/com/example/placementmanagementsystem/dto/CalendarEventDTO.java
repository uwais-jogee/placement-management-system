package com.example.placementmanagementsystem.dto;

/**
 * Data Transfer Object for Calendar Events displayed by FullCalendar JS
 */
public class CalendarEventDTO {

    private Long id;
    private String title;
    private String start;
    private String end;
    private String location;

    public CalendarEventDTO(Long id, String title, String start, String end, String location) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
        this.location = location;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}