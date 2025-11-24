/**
 * @file placementEvaluationForm.js
 * @description JavaScript for the student placement evaluation form.
 */


/**
 * Validates the form and shows the confirmation modal if the form is valid, otherwise displays the error feedback
 */
function validateForm() {
    const form = document.getElementById("placementEvaluationForm");
    const confirmModal = document.getElementById("modal");
    if (form.checkValidity()) {
        showModal(confirmModal);
    } else {
        form.reportValidity();
    }
}


/**
 *  Submits the form after setting the hidden inputs to the values of the form inputs. Called when the user click the confirm button in the modal.
 */
function confirmFormSubmit() {
    const form = document.getElementById("placementEvaluationForm");
    form.submit(); // Submit the form once confirmed in the modal
}


/**
 * When the page loads, adds event listeners to the buttons on the page.
 */
document.addEventListener("DOMContentLoaded", () => {
    // Submit button on the form
    const submitButton = document.getElementById("submitButton");
    submitButton.addEventListener("click", validateForm);

    // Confirm button in the modal
    const confirmButton = document.getElementById("confirmModalButton");
    confirmButton.addEventListener("click", confirmFormSubmit);

    // Go back button in the modal
    const goBackModalButton = document.getElementById("goBackModalButton");
    const modal = document.getElementById("modal");
    goBackModalButton.addEventListener("click" , () => hideModal(modal));
});