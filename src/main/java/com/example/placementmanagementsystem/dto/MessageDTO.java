package com.example.placementmanagementsystem.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Messages between Students and Tutors
 */
public class MessageDTO {

    private Long id;
    private String content;
    private String senderUsername;
    private String senderFirstName;
    private String senderLastName;
    private String receiverUsername;
    private String receiverFirstName;
    private String receiverLastName;
    private Long placementId;
    private LocalDateTime dateTime;

    public MessageDTO(Long id, String content, String senderUsername, String senderFirstName, String senderLastName, String receiverUsername, String receiverFirstName, String receiverLastName, Long placementId, LocalDateTime dateTime) {
        this.id = id;
        this.content = content;
        this.senderUsername = senderUsername;
        this.senderFirstName = senderFirstName;
        this.senderLastName = senderLastName;
        this.receiverUsername = receiverUsername;
        this.receiverFirstName = receiverFirstName;
        this.receiverLastName = receiverLastName;
        this.placementId = placementId;
        this.dateTime = dateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderFirstName() {
        return senderFirstName;
    }

    public void setSenderFirstName(String senderFirstName) {
        this.senderFirstName = senderFirstName;
    }

    public String getSenderLastName() {
        return senderLastName;
    }

    public void setSenderLastName(String senderLastName) {
        this.senderLastName = senderLastName;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getReceiverFirstName() {
        return receiverFirstName;
    }

    public void setReceiverFirstName(String receiverFirstName) {
        this.receiverFirstName = receiverFirstName;
    }

    public String getReceiverLastName() {
        return receiverLastName;
    }

    public void setReceiverLastName(String receiverLastName) {
        this.receiverLastName = receiverLastName;
    }

    public Long getPlacementId() {
        return placementId;
    }

    public void setPlacementId(Long placementId) {
        this.placementId = placementId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}