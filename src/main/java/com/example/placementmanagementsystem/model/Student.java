package com.example.placementmanagementsystem.model;

import com.example.placementmanagementsystem.enumeration.Role;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Student user model class, extends User class.
 * Contains a list of Placement, and PlacementAuthRequest objects that is associated with the student
 */
@Entity
public class Student extends User {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "student_username")
    private List<Placement> placements = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "student_username")
    private List<PlacementAuthRequest> placementAuthRequests = new ArrayList<>();

    /**
     * Default constructor, sets the User role to Student
     */
    public Student() {
        super();
        this.setRole(Role.ROLE_STUDENT);
    }

    public List<Placement> getPlacements() {
        return placements;
    }

    public void setPlacements(List<Placement> placements) {
        this.placements = placements;
    }

    public List<PlacementAuthRequest> getPlacementAuthRequests() {
        return placementAuthRequests;
    }

    public void setPlacementAuthRequests(List<PlacementAuthRequest> placementAuthRequests) {
        this.placementAuthRequests = placementAuthRequests;
    }

    public void addPlacementAuthRequest(PlacementAuthRequest placementAuthRequest) {
        placementAuthRequests.add(placementAuthRequest);
    }
}