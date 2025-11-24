package com.example.placementmanagementsystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Address model class to store a location's address details in Google Maps API format - with placeId and formattedAddress
 */
@Entity
public class Address {

    @Id
    private String placeId;
    private String formattedAddress;

    public Address() {
    }

    public Address(String placeId, String formattedAddress) {
        this.placeId = placeId;
        this.formattedAddress = formattedAddress;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}