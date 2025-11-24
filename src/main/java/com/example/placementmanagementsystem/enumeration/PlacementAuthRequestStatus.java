package com.example.placementmanagementsystem.enumeration;

/**
 * Enumeration to represent the status of a placement authorisation request
 */
public enum PlacementAuthRequestStatus {

    // Placement authorisation request has been submitted
    PENDING_INITIAL_ADMIN_APPROVAL,

    // Requires company approval
    PENDING_COMPANY_APPROVAL,

    // Requires final approval from admin
    PENDING_FINAL_ADMIN_APPROVAL,

    // Placement authorisation request has been approved
    APPROVED,

    // Admin has initially rejected the placement authorisation request
    REJECTED_INITIAL_BY_ADMIN,

    // Employer has rejected the placement authorisation request
    REJECTED_BY_COMPANY,

    // Admin has rejected the placement authorisation request at final stage
    REJECTED_FINAL_BY_ADMIN;

    /**
     * Provides a formatted, user-friendly version of the status
     *
     * @return The status as a string, with the first letter of each word capitalised and a space between words
     */
    public String getFormattedStatus() {
        String[] words = this.name().split("_");
        StringBuilder output = new StringBuilder();
        for (String word : words) {
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