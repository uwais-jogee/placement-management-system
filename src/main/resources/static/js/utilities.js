/**
 * @file utilities.js
 * @description Utility functions for the application
 */


/**
 * Generate the chart colours from a defined theme, based on the number of categories.
 * @param numberOfCategories The number of categories to generate colours for
 * @returns {*[]} An array of colours to use in the chart
 */
function generateChartColours(numberOfCategories) {
    const themeColours = ["#0d9488", "#14b8a6", "#0f766e", "#2dd4bf", "#115e59", "#5eead4", "#134e4a", "#5eead4", "#042f2e"]
    const colours = [];
    for (let i = 0; i < numberOfCategories; i++) {
        colours.push(themeColours[i % themeColours.length]); // Cycle through the theme colors
    }
    return colours;
}


/**
 * Show the given modal
 * @param modalEl The modal element to show
 */
function showModal(modalEl) {
    modalEl.classList.remove("hidden");
}


/**
 * Hide the given modal
 * @param modalEl The modal element to hide
 */
function hideModal(modalEl) {
    modalEl.classList.add("hidden");
}


/**
 * Toggle the visibility of the password input
 * @param passwordInput The password input element
 * @param eyeIcon The eye show password icon
 * @param eyeSlashIcon The eye slash hide password icon
 */
function togglePasswordVisibility(passwordInput, eyeIcon, eyeSlashIcon) {
    if (passwordInput.type === "password") {
        passwordInput.type = "text";
        eyeIcon.classList.add("hidden");
        eyeSlashIcon.classList.remove("hidden");
    } else {
        passwordInput.type = "password";
        eyeIcon.classList.remove("hidden");
        eyeSlashIcon.classList.add("hidden");
    }
}


/**
 * Check the password input against the password validation checks and update the UI
 * @param password The password to check
 * @param passwordValidation The password validation div element to update
 * @returns {boolean} True if all checks pass, false otherwise
 */
function checkPasswordStrength(password, passwordValidation) {
    const lengthCheck = passwordValidation.querySelector("#lengthCheck");
    const upperCheck = passwordValidation.querySelector("#upperCheck");
    const lowerCheck = passwordValidation.querySelector("#lowerCheck");
    const numberCheck = passwordValidation.querySelector("#numberCheck");
    const specialCheck = passwordValidation.querySelector("#specialCheck");

    // Validate the password
    lengthCheck.checked = password.length >= 8;
    upperCheck.checked = /[A-Z]/.test(password);
    lowerCheck.checked = /[a-z]/.test(password);
    numberCheck.checked = /\d/.test(password);
    specialCheck.checked = /[@$!%*?&#]/.test(password);

    // Return true if all checks pass
    return lengthCheck.checked && upperCheck.checked && lowerCheck.checked && numberCheck.checked && specialCheck.checked;
}