package com.example.placementmanagementsystem.enumeration;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * Enumeration representing the roles that a user can have.
 * Implements the GrantedAuthority interface to allow Spring Security to use this enum as a role
 */
public enum Role implements GrantedAuthority {

    // Placement student
    ROLE_STUDENT,

    // Placement tutor
    ROLE_TUTOR,

    // Placement administrator
    ROLE_ADMIN,

    // User accounts manager - able to create, update, and delete user accounts
    ROLE_USER_MANAGER;

    /**
     * Overridden method from the GrantedAuthority interface that returns the user role as a string
     *
     * @return The user role as a string
     */
    @Override
    public String getAuthority() {
        return this.name();
    }

    /**
     * Provides a formatted, user-friendly version of the status
     *
     * @return The user role as a string, without the ROLE_ prefix and with each word capitalised.
     */
    public String getFormattedRole() {
        List<String> words = List.of(this.name().split("_"));
        StringBuilder output = new StringBuilder();
        // Iterate over each word in the role name, ignoring the first word 'ROLE'
        for (String word : words.subList(1, words.size())) {
            // Capitalize the first letter and convert the rest to lowercase
            if (word.length() > 0) {
                output.append(Character.toUpperCase(word.charAt(0))); // Capitalize first letter
                output.append(word.substring(1).toLowerCase()); // Add the rest of the word in lowercase
            }
            output.append(" "); // Add space between words
        }
        // Remove the trailing space and return the result
        return output.toString().trim();
    }
}