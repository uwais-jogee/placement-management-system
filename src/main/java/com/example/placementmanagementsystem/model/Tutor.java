package com.example.placementmanagementsystem.model;

import com.example.placementmanagementsystem.enumeration.Role;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Tutor user model class, extends User class.
 * Contains a list of Placement objects that the tutor is assigned to
 */
@Entity
public class Tutor extends User {

    @OneToMany(mappedBy = "tutor", orphanRemoval = false, fetch = FetchType.EAGER)
    private List<Placement> placements = new ArrayList<>();

    /**
     * Default constructor, sets the User role to Tutor
     */
    public Tutor() {
        super();
        this.setRole(Role.ROLE_TUTOR);
    }

    public List<Placement> getPlacements() {
        return placements;
    }

    public void setPlacements(List<Placement> placements) {
        this.placements = placements;
    }
}