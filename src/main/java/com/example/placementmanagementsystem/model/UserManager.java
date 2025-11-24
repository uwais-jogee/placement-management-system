package com.example.placementmanagementsystem.model;

import com.example.placementmanagementsystem.enumeration.Role;
import jakarta.persistence.Entity;

/**
 * User Manager user model class, extends User class
 */
@Entity
public class UserManager extends User {

    /**
     * Default constructor, sets the User role to User Manager
     */
    public UserManager() {
        super();
        this.setRole(Role.ROLE_USER_MANAGER);
    }
}