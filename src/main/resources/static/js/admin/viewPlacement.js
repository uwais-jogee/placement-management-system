/**
 * @file viewPlacement.js
 * @description JavaScript for the view placement page
 */


/**
 * Submits the change tutor form if it is valid.
 * If the form is invalid, the selected tutor is the same as the old tutor, displays the error message to the user.
 */
function onChangeTutorSubmit() {
    const changeTutorForm = document.getElementById("changeTutorForm");
    const oldTutorUsername = document.getElementById("oldTutorUsername").value;
    const newTutorSelect = document.getElementById("newTutorSelect");
    const newTutorUsername = newTutorSelect.value;

    // Check the selected username is different to the current tutor's username
    if (oldTutorUsername === newTutorUsername) {
        newTutorSelect.setCustomValidity("Please select a different tutor to the current tutor");
    } else {
        newTutorSelect.setCustomValidity("");
    }

    // Check the form is valid
    if (changeTutorForm.checkValidity()) {
        // Submit the form
        changeTutorForm.submit();
    } else {
        changeTutorForm.reportValidity();
    }
}


/**
 * When the new tutor select box changes, checks if the username selected is different to the current tutor's username.
 * If the selected tutor is the same as the old tutor, disables the submit button.
 * If the selected tutor is different to the old tutor, enables the submit button.
 */
function onNewTutorSelectChange() {
    const changeTutorModalSubmit = document.getElementById("changeTutorModalSubmit");
    const oldTutorUsername = document.getElementById("oldTutorUsername").value;
    const newTutorSelect = document.getElementById("newTutorSelect");
    const newTutorUsername = newTutorSelect.value;

    // Disable the submit button if the selected tutor is the same as the old tutor
    changeTutorModalSubmit.disabled = oldTutorUsername === newTutorUsername;
}


/**
 * When the document has loaded, adds event listeners to modal buttons
 */
document.addEventListener("DOMContentLoaded", function () {
    const changeTutorModal = document.getElementById("changeTutorModal");
    const goBackChangeTutorModal = document.getElementById("goBackChangeTutorModal");
    const changeTutorModalSubmit = document.getElementById("changeTutorModalSubmit");
    const changeTutorButton = document.getElementById("changeTutorButton");
    const newTutorSelect = document.getElementById("newTutorSelect");

    // Event listener for the change tutor button
    changeTutorButton.addEventListener("click", () => {
        // Set the form option value to the current tutor
        const oldTutorUsername = document.getElementById("oldTutorUsername").value;
        const newTutorSelect = document.getElementById("newTutorSelect")

        // Set the current tutor as the selected value
        newTutorSelect.value = oldTutorUsername;

        onNewTutorSelectChange();
        showModal(changeTutorModal);
    });

    // Event listener for the change tutor modal select box
    newTutorSelect.addEventListener("change", () => onNewTutorSelectChange());

    // Event listener for the change tutor modal submit button
    changeTutorModalSubmit.addEventListener("click", () => onChangeTutorSubmit());

    // Event listener for the go back button on the change tutor modal
    goBackChangeTutorModal.addEventListener("click", () => hideModal(changeTutorModal));
});