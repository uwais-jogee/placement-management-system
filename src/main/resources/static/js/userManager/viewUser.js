/**
 * @file viewUser.js
 * @description JavaScript for the user manager, view user page.
 */


/**
 * AJAX request to check if the email is available.
 * Displays the error message if the username is not available, and updates the submit button.
 */
function validateEmail() {
    const editEmail = document.getElementById("editEmail");
    const editEmailError = document.getElementById("editEmailError");
    const submitEditModal = document.getElementById("submitEditModal");
    const currentEmail = document.getElementById("email").textContent;

    // AJAX request to check if the username is available
    fetch("/user-manager/user/new/validate-email?email=" + editEmail.value + "&currentEmail=" + currentEmail, {
        method: "POST", headers: {
            "Content-Type": "application/json"
        },
    })
        .then(response => response.json())
        .then(data => {
            if (data.available) {
                // Hide the error message
                editEmailError.classList.add("hidden");
                // Change ring color back to gray
                editEmail.classList.remove("ring-red-300");
                editEmail.classList.add("ring-gray-300");
                submitEditModal.disabled = false;
            } else {
                // Show the error message
                editEmailError.classList.remove("hidden");
                // Change ring color to red
                editEmail.classList.remove("ring-gray-300");
                editEmail.classList.add("ring-red-300");
                submitEditModal.disabled = true;
            }
        })
        .catch(error => console.error("Error checking email availability:", error));
}


/**
 * On page load, adds event listeners to the action buttons, modal buttons, and form inputs.
 */
document.addEventListener("DOMContentLoaded", function () {
    // Actions buttons
    const editButton = document.getElementById("editButton");
    const disableButton = document.getElementById("disableButton");
    const enableButton = document.getElementById("enableButton");
    const deleteButton = document.getElementById("deleteButton");
    const resendActivationEmailButton = document.getElementById("resendActivationEmailButton");
    // Modals
    const editModal = document.getElementById("editModal");
    const disableModal = document.getElementById("disableModal");
    const enableModal = document.getElementById("enableModal");
    const deleteModal = document.getElementById("deleteModal");
    const resendActivationEmailModal = document.getElementById("resendActivationEmailModal");
    // Modal cancel buttons
    const cancelEditModal = document.getElementById("cancelEditModal");
    const cancelDisableModal = document.getElementById("cancelDisableModal");
    const cancelEnableModal = document.getElementById("cancelEnableModal");
    const cancelDeleteModal = document.getElementById("cancelDeleteModal");
    const cancelResendActivationEmailModal = document.getElementById("cancelResendActivationEmailModal");

    // Edit
    const editForm = document.getElementById("editForm");
    editButton.addEventListener("click", () => showModal(editModal));
    cancelEditModal.addEventListener("click", () => {
        hideModal(editModal);
        editForm.reset();
    });
    // On input listener for the email field to validate the email is available
    const editEmail = document.getElementById("editEmail");
    editEmail.addEventListener("input", validateEmail);

    // Disable
    if (disableButton) {
        disableButton.addEventListener("click", () => showModal(disableModal));
        cancelDisableModal.addEventListener("click", () => hideModal(disableModal));
    }

    // Enable
    if (enableButton) {
        enableButton.addEventListener("click", () => showModal(enableModal));
        cancelEnableModal.addEventListener("click", () => hideModal(enableModal));
    }

    // Delete
    deleteButton.addEventListener("click", () => showModal(deleteModal));
    cancelDeleteModal.addEventListener("click", () => hideModal(deleteModal));

    // Resend activation email
    if (resendActivationEmailButton) {
        resendActivationEmailButton.addEventListener("click", () => showModal(resendActivationEmailModal));
        cancelResendActivationEmailModal.addEventListener("click", () => hideModal(resendActivationEmailModal));
    }
});