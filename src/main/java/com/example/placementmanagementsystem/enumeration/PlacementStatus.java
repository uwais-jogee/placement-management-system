package com.example.placementmanagementsystem.enumeration;

/**
 * Enumeration to represent the status of a placement
 */
public enum PlacementStatus {

    // Placement is upcoming and has not started yet
    UPCOMING,

    // Placement is in progress
    IN_PROGRESS,

    // Placement has ended and the student must complete an evaluation form
    PENDING_STUDENT_EVALUATION,

    // Evaluation completed by student and the placement has ended
    COMPLETED;

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