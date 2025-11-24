/**
 * @file myPlacement.js
 * @description JavaScript for the student my placement page
 */


/**
 * Restricts the date picker inputs in the date change modal, depending on the placement status and current date
 */
function updateDateChangeModalInputs() {
    const status = document.getElementById("placementStatus").textContent;
    const startDateInput = document.getElementById("newStartDate");
    const endDateInput = document.getElementById("newEndDate");

    const today = new Date();

    // If the placement is upcoming, both start and end dates can be changed
    if (status === "Upcoming") {
        // Set the minimum date for the start and end date inputs to today
        startDateInput.min = today.toISOString().split("T")[0];
        endDateInput.min = today.toISOString().split("T")[0];
    } else if (status === "In Progress") {
        // If the placement is in progress, only the end date can be changed, and it must be after the start date
        startDateInput.disabled = true;
        endDateInput.min = startDateInput.value;
    } else { // This should not be the case as the modal will not be accessible
        // Otherwise, the placement is completed or pending evaluation and no changes can be made
        startDateInput.disabled = true;
        endDateInput.disabled = true;
    }

    // Set the max end date to be the end of September - the start of the next academic year
    endDateInput.max = new Date(startDateInput.value).getFullYear() + 1 + "-09-30";
}

/**
 * Validate the date change form. If valid, submits the form. If invalid, displays the error feedback.
 */
function submitDateChange() {
    const form = document.getElementById("dateChangeForm");
    const startDate = document.getElementById("newStartDate").value;
    const endDate = document.getElementById("newEndDate").value;
    const status = document.getElementById("placementStatus").textContent;

    // If placement is upcoming either start date or end date can be changed
    // If placement is in progress, only end date can be changed
    // If placement is completed/evaluation pending, no changes can be made

    // Start must be before end
    // If start date is changed, it must be in the future
    // If end date is changed, it must be after the start date and in the future

    // Check if the form is valid
    if (form.checkValidity()) {
        // Validate the dates
        if (status === "Upcoming") {
            // Either date may have changed
            if (new Date(startDate) < new Date(endDate)) {
                form.submit();
            } else {
                // Set custom validation message
                document.getElementById("newStartDate").setCustomValidity("Start date must be before end date");
                document.getElementById("newEndDate").setCustomValidity("End date must be after start date");
                form.reportValidity();
            }
        } else if (status === "In Progress") {
            // Only end date may have changed
            if (new Date(startDate) < new Date(endDate)) {
                // Enable the start date input to submit the form
                document.getElementById("newStartDate").disabled = false;
                form.submit();
            } else {
                // Set custom validation message
                document.getElementById("newEndDate").setCustomValidity("End date must be after start date");
                form.reportValidity();
            }
        } else {
            console.log("Placement is completed or pending evaluation. No changes can be made.");
        }
    } else {
        // Remove custom validation message
        document.getElementById("newStartDate").setCustomValidity("");
        document.getElementById("newEndDate").setCustomValidity("");
        form.reportValidity();
    }
}


document.addEventListener("DOMContentLoaded", () => {
    const dateChangeButton = document.getElementById("dateChangeButton");
    const dateChangeModal = document.getElementById("dateChangeModal");
    const dateChangeModalConfirmButton = document.getElementById("dateChangeModalConfirmButton");
    const dateChangeModalBackButton = document.getElementById("dateChangeModalBackButton");

    // If the data change components have been rendered, i.e. the student has a placement, add event listeners to the modal buttons
    if (dateChangeButton && dateChangeModal && dateChangeModalConfirmButton && dateChangeModalBackButton) {
        dateChangeButton.addEventListener("click", () => showModal(dateChangeModal));
        dateChangeModalBackButton.addEventListener("click", () => hideModal(dateChangeModal));
        dateChangeModalConfirmButton.addEventListener("click", submitDateChange);
        // Update the date change inputs restrictions
        updateDateChangeModalInputs();
    }
});