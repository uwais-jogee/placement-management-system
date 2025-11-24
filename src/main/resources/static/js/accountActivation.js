/**
 * @file accountActivation.js
 * @description JavaScript for the account activation page
 */


/**
 * Validates the password and repeat password fields
 * Checks they are the same, and the password is strong
 * Updated the UI and submit button
 */
function validatePassword() {
    const password = document.getElementById("password");
    const repeatPassword = document.getElementById("repeatPassword");
    const repeatPasswordError = document.getElementById("repeatPasswordError");
    const submitActivateAccount = document.getElementById("submitActivateAccount");
    const passwordValidation = document.getElementById("passwordValidation")
    const form = document.getElementById("accountActivationForm");

    // Check the password strength
    const strong = checkPasswordStrength(password.value, passwordValidation);

    // Check if the password and repeat password match
    let match = false;
    if (password.value !== "" && repeatPassword.value !== "" && password.value !== repeatPassword.value) {
        // Show the error message
        repeatPasswordError.classList.remove("hidden");
        // Change ring color to red
        repeatPassword.classList.remove("ring-gray-300");
        repeatPassword.classList.add("ring-red-300");
        match = false;
    } else {
        // Hide the error message
        repeatPasswordError.classList.add("hidden");
        // Change ring color back to gray
        repeatPassword.classList.remove("ring-red-300");
        repeatPassword.classList.add("ring-gray-300");
        match = true;
    }

    // Enable/disable the submit button if the form is valid, and strong and match are true
    submitActivateAccount.disabled = !(form.checkValidity() && strong && match);
}


/**
 * When the document is loaded, add event listeners to the password and repeat password fields to validate the passwords are the same, and show/hide the password.
 */
document.addEventListener("DOMContentLoaded", function () {
    const passwordInput = document.getElementById("password");
    const repeatPasswordInput = document.getElementById("repeatPassword");
    const eyeIcon = document.getElementById("eyeIcon");
    const eyeSlashIcon = document.getElementById("eyeSlashIcon");
    const repeatEyeIcon = document.getElementById("repeatEyeIcon");
    const repeatEyeSlashIcon = document.getElementById("repeatEyeSlashIcon");

    // On input listeners for the password and repeat password fields to validate the passwords are the same
    passwordInput.addEventListener("input", validatePassword);
    repeatPasswordInput.addEventListener("input", validatePassword);

    // On click listener for the show and hide password button
    eyeIcon.addEventListener("click", () => togglePasswordVisibility(passwordInput, eyeIcon, eyeSlashIcon));
    eyeSlashIcon.addEventListener("click", () => togglePasswordVisibility(passwordInput, eyeIcon, eyeSlashIcon));

    // On click listener for the show and hide repeat password button
    repeatEyeIcon.addEventListener("click", () => togglePasswordVisibility(repeatPasswordInput, repeatEyeIcon, repeatEyeSlashIcon));
    repeatEyeSlashIcon.addEventListener("click", () => togglePasswordVisibility(repeatPasswordInput, repeatEyeIcon, repeatEyeSlashIcon));
});