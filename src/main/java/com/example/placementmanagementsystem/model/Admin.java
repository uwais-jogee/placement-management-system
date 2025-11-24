package com.example.placementmanagementsystem.model;

import com.example.placementmanagementsystem.enumeration.Role;
import jakarta.persistence.Entity;

/**
 * Admin user model class, extends User class
 */
@Entity
public class Admin extends User {

    /**
     * Default constructor, sets the User role to Admin
     */
    public Admin() {
        super();
        this.setRole(Role.ROLE_ADMIN);
    }
}