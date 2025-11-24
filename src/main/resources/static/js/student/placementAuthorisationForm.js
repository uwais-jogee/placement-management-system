/**
 * @file placementAuthorisationForm.js
 * @description JavaScript for the student  placement authorisation form
 */


// Global variables
let autocomplete;


/**
 * Initialises the Google Places API autocomplete for the company address input
 */
function initGoogle() {
    autocomplete = new google.maps.places.Autocomplete(
        document.getElementById("companyAddress"),
        {
            types: ["address"],
            componentRestrictions: {country: ["uk"]},
            fields: ["place_id", "formatted_address", "geometry"]
        });

    // Add event listeners for place changed and input events to check the validity of the address
    autocomplete.addListener("place_changed", () => onPlaceChanged(true));
    document.getElementById("companyAddress").addEventListener("input", () => onPlaceChanged(false));
}


/**
 * Checks if a valid place has been selected. If valid, removed the custom validity message. If not, sets a custom validity message.
 * @param placeSelected A flag to check if a place has been selected by the user. True if a place has been selected, false if not selected and the user is typing
 */
function onPlaceChanged(placeSelected) {
    const companyAddress = document.getElementById("companyAddress");
    const place = autocomplete.getPlace();
    if (place && placeSelected === true) {
        console.log("Place found");
        // If a place has been selected, clear the custom validity message
        companyAddress.setCustomValidity("");
    } else {
        console.log("Place not found");
        // If a place has not been selected, set a custom validity message
        companyAddress.setCustomValidity("Please select an address from the dropdown");
    }
}


/**
 * Sets the select inputs to empty value, so there is no selected option on page load
 */
function setInputsToEmpty() {
    document.getElementById("programmeOfStudy").value = "";
    document.getElementById("companyIndustry").value = "";
    document.getElementById("placementStartDate").value = "";
    document.getElementById("internationalStudent").value = "";
    document.getElementById("visaStatus").value = "";
    document.getElementById("companyNameSelect").value = "";
    document.getElementById("remote").value = "";
    document.getElementById("travelArrangements").value = "";
    document.getElementById("residentialArrangements").value = "";
    document.getElementById("personalAdjustments").value = "";
}


/**
 * Sets the minimum date for the end date input to the start date + 1 day - called when the start date is changed
 */
function setEndDateMin() {
    const startDate = document.getElementById("placementStartDate").value;
    // Set the minimum date for the end date to the start date + 1 day
    document.getElementById("placementEndDate").min = new Date(new Date(startDate).setDate(new Date(startDate).getDate() + 1)).toISOString().split('T')[0];
}


/**
 * Toggles the visibility of the visa status section based on the value of the international student select input
 */
function onInternationStudentChange() {
    const visaStatusSection = document.getElementById("visaStatusSection");
    const visaStatus = document.getElementById("visaStatus");
    const internationalStudent = document.getElementById("internationalStudent");

    if (internationalStudent.value === "Yes") {
        visaStatusSection.classList.remove("hidden");
        visaStatus.value = "";
        document.getElementById("visaStatus").required = true;
    } else {
        visaStatusSection.classList.add("hidden");
        visaStatus.value = "";
        document.getElementById("visaAlert").classList.add("hidden");
        document.getElementById("visaStatus").required = false;
    }
    onVisaStatusChange();
}


/**
 * Toggles the visibility of the company name section based on the value of the remote select input.
 * If the value is "Other", the company name other inputs are shown for the user to input a custom company.
 * If the value is not "Other", the company name other inputs are hidden and the company details are pre-filled.
 */
function toggleCompanyName() {
    // The entire section for when 'Other' is selected
    const companyNameOtherInputSection = document.getElementById("companyNameOtherInputSection");
    // The selected company
    const companyNameSelect = document.getElementById("companyNameSelect");
    const selectedCompany = companyNameSelect.options[companyNameSelect.selectedIndex];
    //  The input for the custom company name
    const companyNameOtherInput = document.getElementById("companyNameOtherInput");
    // Company details inputs
    const companyAddress = document.getElementById("companyAddress");
    const companyIndustry = document.getElementById("companyIndustry");
    const companyWebAddress = document.getElementById("companyWebAddress");

    const breakDiv = document.getElementById("breakDiv"); // Break div to ensure the input is on a new line

    // Remove any custom validity on the address, as the address is reset when the company is changed
    companyAddress.setCustomValidity("");

    if (selectedCompany.value === "Other") {
        breakDiv.classList.add("sm:hidden");
        breakDiv.classList.remove("sm:block");
        // Show the input for the custom company name
        companyNameOtherInputSection.classList.remove("hidden");
        companyNameOtherInput.value = "";
        companyNameOtherInput.required = true;

        // Clear the address, industry and web address
        companyAddress.value = "";
        companyIndustry.value = "";
        companyWebAddress.value = "";

        // Enable the fields for input
        companyAddress.disabled = false;
        companyIndustry.disabled = false;
        companyWebAddress.disabled = false;

    } else {
        breakDiv.classList.remove("sm:block");
        breakDiv.classList.add("sm:hidden");
        // Hide the input for the custom company name
        companyNameOtherInputSection.classList.add("hidden");
        companyNameOtherInput.required = false;
        companyNameOtherInput.value = "";

        // Pre-fill the company details - set to empty if not available
        companyAddress.value = selectedCompany.getAttribute("data-formattedAddress") || "";
        companyIndustry.value = selectedCompany.getAttribute("data-industry") || "";
        companyWebAddress.value = selectedCompany.getAttribute("data-webAddress") || "";

        // Disable fields to prevent editing
        companyAddress.disabled = true;
        companyIndustry.disabled = true;
        companyWebAddress.disabled = true;
    }
}


/**
 * Displays an alert if the visa status is set to "No" and disables the submit button, otherwise hides the alert and enables the submit button
 */
function onVisaStatusChange() {
    const visaStatus = document.getElementById("visaStatus").value;

    if (visaStatus === "No") {
        document.getElementById("submitButton").disabled = true;
        document.getElementById("visaAlert").classList.remove("hidden");
    } else {
        document.getElementById("submitButton").disabled = false;
        document.getElementById("visaAlert").classList.add("hidden");
    }
}


/**
 * Displays an alert if the personal adjustments are set to "No" and disables the submit button, otherwise hides the alert and enables the submit button
 */
function onPersonalAdjustmentsChange() {
    const personalAdjustments = document.getElementById("personalAdjustments").value;
    if (personalAdjustments === "No") {
        document.getElementById("submitButton").disabled = true;
        document.getElementById("personalAdjustmentsAlert").classList.remove("hidden");
    } else {
        document.getElementById("submitButton").disabled = false;
        document.getElementById("personalAdjustmentsAlert").classList.add("hidden");
    }
}


/**
 * Sets the hidden input fields, called when the form is submitted
 */
function setHiddenInputs() {
    const companyNameSelect = document.getElementById("companyNameSelect");
    const companyAddress = document.getElementById("companyAddress");
    const companyPlaceId = document.getElementById("companyPlaceId");
    const companyFormattedAddress = document.getElementById("companyFormattedAddress");
    const companyIndustry = document.getElementById("companyIndustry");
    const companyWebAddress = document.getElementById("companyWebAddress");
    const internationalStudent = document.getElementById("internationalStudent");
    const visaStatus = document.getElementById("visaStatus");

    // Set hidden input values for company address using Google Places API
    if (companyNameSelect.value === "Other") {
        companyPlaceId.value = autocomplete.getPlace().place_id;
        companyFormattedAddress.value = autocomplete.getPlace().formatted_address;
    } else {
        // Ensure they are not disabled so the values are sent
        companyAddress.disabled = false;
        companyIndustry.disabled = false;
        companyWebAddress.disabled = false;
        companyPlaceId.value = companyNameSelect.options[companyNameSelect.selectedIndex].getAttribute("data-placeId");
        companyFormattedAddress.value = companyNameSelect.options[companyNameSelect.selectedIndex].getAttribute("data-formattedAddress");
    }

    // Set the visa status to N/A (hidden) if the student is not international
    if (internationalStudent.value === "No") {
        visaStatus.querySelector("option[value='N/A']").hidden = false;
        visaStatus.value = "N/A";
    }
}


/**
 *  Submits the form after setting the hidden inputs to the values of the form inputs. Called when the user click the confirm button in the modal.
 */
function confirmFormSubmit() {
    const form = document.getElementById("placementAuthRequestForm");
    setHiddenInputs();
    form.submit(); // Submit the form once confirmed in the modal
}


/**
 * Validates the form and shows the confirmation modal if the form is valid, otherwise displays the error feedback
 */
function validateForm() {
    const form = document.getElementById("placementAuthRequestForm");
    const confirmModal = document.getElementById("modal");
    if (form.checkValidity()) {
        showModal(confirmModal);
    } else {
        form.reportValidity();
    }
}


/**
 * When the page loads, clears the inputs, sets the start date minimum to today, and adds the event listeners to the buttons and inputs
 */
document.addEventListener("DOMContentLoaded", () => {
    // Clear the inputs on page load
    setInputsToEmpty();

    // Set the start date minimum to today and add an event listener to change the end date minimum when the start date is changed
    const placementStartDate = document.getElementById("placementStartDate");
    placementStartDate.min = new Date().toISOString().split('T')[0];
    placementStartDate.addEventListener("change", setEndDateMin);

    // Button and Input event listeners
    const submitButton = document.getElementById("submitButton");
    submitButton.addEventListener("click", validateForm);

    const confirmModalButton = document.getElementById("confirmModalButton");
    confirmModalButton.addEventListener("click", confirmFormSubmit);

    const goBackModalButton = document.getElementById("goBackModalButton");
    const modal = document.getElementById("modal");
    goBackModalButton.addEventListener("click" , () => hideModal(modal));

    const internationalStudent = document.getElementById("internationalStudent");
    internationalStudent.addEventListener("change", onInternationStudentChange);

    const visaStatus = document.getElementById("visaStatus");
    visaStatus.addEventListener("change", onVisaStatusChange);

    const personalAdjustments = document.getElementById("personalAdjustments");
    personalAdjustments.addEventListener("change", onPersonalAdjustmentsChange);

    const companyNameSelect = document.getElementById("companyNameSelect");
    companyNameSelect.addEventListener("change", toggleCompanyName);
});