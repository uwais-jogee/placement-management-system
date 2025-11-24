/**
 * @file profile.js
 * @description JavaScript for profile page for all user roles
 */


/**
 * Validates the password and repeat password fields
 * Checks they are the same, and the password is strong
 * Updated the UI and submit button
 */
function validateNewPassword() {
    const newPassword = document.getElementById("newPassword");
    const repeatNewPassword = document.getElementById("repeatNewPassword");
    const repeatNewPasswordError = document.getElementById("repeatNewPasswordError");
    const submitChangePasswordModal = document.getElementById("submitChangePasswordModal");
    const passwordValidation = document.getElementById("passwordValidation");
    const form = document.getElementById("changePasswordForm");

    const strong = checkPasswordStrength(newPassword.value, passwordValidation);

    let match = false;
    if (newPassword.value !== "" && repeatNewPassword.value !== "" && newPassword.value !== repeatNewPassword.value) {
        // Show the error message
        repeatNewPasswordError.classList.remove("hidden");
        // Change ring color to red
        repeatNewPassword.classList.remove("ring-gray-300");
        repeatNewPassword.classList.add("ring-red-300");
        match = false;
    } else {
        // Hide the error message
        repeatNewPasswordError.classList.add("hidden");
        // Change ring color back to gray
        repeatNewPassword.classList.remove("ring-red-300");
        repeatNewPassword.classList.add("ring-gray-300");
        match = true;
    }

    // Enable/disable the submit button if the form is valid, and strong and match are true
    submitChangePasswordModal.disabled = !(form.checkValidity() && strong && match);
}


/**
 * When the document is loaded, add event listeners to the change password button, new password field, repeat new password field, and show/hide each of the passwords
 */
document.addEventListener("DOMContentLoaded", function () {
    // On click listener to open change password modal
    const changePasswordButton = document.getElementById("changePasswordButton");
    const changePasswordModal = document.getElementById("changePasswordModal");
    changePasswordButton.addEventListener("click", () => showModal(changePasswordModal));

    // On click listener to close change password modal and reset the form
    const cancelChangePasswordModal = document.getElementById("cancelChangePasswordModal");
    const changePasswordForm = document.getElementById("changePasswordForm");
    cancelChangePasswordModal.addEventListener("click", () => {
        hideModal(changePasswordModal);
        changePasswordForm.reset();
    });

    // On input listeners for the password and repeat password fields to validate the passwords are the same
    const newPassword = document.getElementById("newPassword");
    const repeatNewPassword = document.getElementById("repeatNewPassword");
    newPassword.addEventListener("input", validateNewPassword);
    repeatNewPassword.addEventListener("input", validateNewPassword);

    // On click listener to show/hide the current password
    const currentPassword = document.getElementById("currentPassword");
    const currentEyeIcon = document.getElementById("currentEyeIcon");
    const currentEyeSlashIcon = document.getElementById("currentEyeSlashIcon");
    currentEyeIcon.addEventListener("click", () => togglePasswordVisibility(currentPassword, currentEyeIcon, currentEyeSlashIcon));
    currentEyeSlashIcon.addEventListener("click", () => togglePasswordVisibility(currentPassword, currentEyeIcon, currentEyeSlashIcon));

    // On click listener to show/hide the new password
    const newEyeIcon = document.getElementById("newEyeIcon");
    const newEyeSlashIcon = document.getElementById("newEyeSlashIcon");
    newEyeIcon.addEventListener("click", () => togglePasswordVisibility(newPassword, newEyeIcon, newEyeSlashIcon));
    newEyeSlashIcon.addEventListener("click", () => togglePasswordVisibility(newPassword, newEyeIcon, newEyeSlashIcon));

    // On click listener to show/hide the repeat new password
    const repeatNewEyeIcon = document.getElementById("repeatNewEyeIcon");
    const repeatNewEyeSlashIcon = document.getElementById("repeatNewEyeSlashIcon");
    repeatNewEyeIcon.addEventListener("click", () => togglePasswordVisibility(repeatNewPassword, repeatNewEyeIcon, repeatNewEyeSlashIcon));
    repeatNewEyeSlashIcon.addEventListener("click", () => togglePasswordVisibility(repeatNewPassword, repeatNewEyeIcon, repeatNewEyeSlashIcon));
});