/**
 * @file login.js
 * @description JavaScript for the login page
 */


/**
 * When the document is loaded, add event listeners to show/hide the password.
 */
document.addEventListener("DOMContentLoaded", function () {
    const eyeIcon = document.getElementById("eyeIcon");
    const eyeSlashIcon = document.getElementById("eyeSlashIcon");
    const passwordInput = document.getElementById("password");

    // On click listener for the show password button
    eyeIcon.addEventListener("click", () => togglePasswordVisibility(passwordInput, eyeIcon, eyeSlashIcon));

    // On click listener for the hide password button
    eyeSlashIcon.addEventListener("click", () => togglePasswordVisibility(passwordInput, eyeIcon, eyeSlashIcon));
});