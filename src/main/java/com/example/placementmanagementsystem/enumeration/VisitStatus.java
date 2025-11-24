package com.example.placementmanagementsystem.enumeration;

public enum VisitStatus {

    // Visit has been scheduled
    UPCOMING,

    // Visit has been completed
    COMPLETED,

    // Visit has been cancelled
    CANCELLED;

    /**
     * Provides a formatted, user-friendly version of the status
     *
     * @return The status as a string, with only the first letter of the word capitalised
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