/**
 * @file app.js
 * @description This file contains the JavaScript used across the application
 */


/**
 * Toggles displaying the mobile navigation menu when the mobile menu button is clicked
 */
function toggleMobileMenu() {
    const mobileMenu = document.getElementById("mobileMenu");
    mobileMenu.classList.toggle("hidden");
}

/**
 * Shows the user menu when the user menu button in the navigation bar is clicked
 */
function showUserMenu() {
    const userMenu = document.getElementById("userMenu");
    userMenu.classList.remove("hidden");
}

/**
 * Closes the user menu when a click event occurs, outside the user menu or the user menu button
 * @param event The click event
 */
function closeUserMenu(event) {
    const userMenu = document.getElementById("userMenu");
    const userMenuButton = document.getElementById("userMenuButton");
    // Check if the click target is not the user menu or the button, then close the user menu
    if (!userMenu.classList.contains("hidden") && !userMenu.contains(event.target) && !userMenuButton.contains(event.target)) {
        userMenu.classList.add("hidden");
    }
}

/**
 * Event listener for the DOMContentLoaded event
 * Initialises the event listeners for the mobile menu button, user menu button and closing the user menu
 */
document.addEventListener("DOMContentLoaded", function () {
    // Onclick listener for the mobile menu button
    const mobileMenuButton = document.getElementById("mobileMenuButton");
    mobileMenuButton.addEventListener("click", toggleMobileMenu);

    // Onclick listener for the user menu button
    const userMenuButton = document.getElementById("userMenuButton");
    userMenuButton.addEventListener("click", showUserMenu);

    // Onclick listener for closing the user menu
    document.addEventListener("click", closeUserMenu);
});