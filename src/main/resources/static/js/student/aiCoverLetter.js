/**
 * @file aiCoverLetter.js
 * @description JavaScript for the Student resource AI Cover Letter page
 */


/**
 * Toggles the loading overlay UI on and off
 */
function toggleLoadingOverlay() {
    const loadingOverlay = document.getElementById("loadingOverlay");
    loadingOverlay.classList.toggle("hidden");
}


/**
 * Scrolls user to the bottom of the page
 */
function scrollToBottom() {
    window.scrollTo(0, document.body.scrollHeight);
}


/**
 * Validates the cover letter form.
 * If the form is valid, getCoverLetter() is called to generate the cover letter.
 * If the form is invalid, the form error messages are displayed.
 */
function validateCoverLetterForm() {
    const coverLetterForm = document.getElementById("coverLetterForm");
    if (coverLetterForm.checkValidity()) {
        getCoverLetter();
    } else {
        coverLetterForm.reportValidity();
    }
}


/**
 * Sends an AJAX request to the server to generate a cover letter.
 * Calls displayCoverLetter() to display the generated cover letter.
 */
function getCoverLetter() {
    const cvInput = document.getElementById("cv");
    const jobTitleInput = document.getElementById("jobTitle");
    const jobDescriptionInput = document.getElementById("jobDescription");
    const companyNameInput = document.getElementById("companyName");
    const generateButton = document.getElementById("generateButton");

    // Create a FormData object
    const formData = new FormData();
    formData.append("cv", cvInput.files[0]);
    formData.append("companyName", companyNameInput.value);
    formData.append("jobTitle", jobTitleInput.value);
    formData.append("jobDescription", jobDescriptionInput.value);

    toggleLoadingOverlay();
    generateButton.disabled = true;

    // Send the AJAX request
    fetch("/student/resources/applying-for-placements/ai-cover-letter/generate", {
        method: "POST", body: formData
    })
        .then(response => response.json())
        .then(data => {
            console.log(data.coverLetter);
            displayCoverLetter(data.coverLetter);
        })
        .catch(error => {
            console.error("Error:", error);
        })
        .finally(() => {
            toggleLoadingOverlay();
            scrollToBottom();
        });
}


/**
 * Displays the generated cover letter on the page.
 * @param coverLetter The generated cover letter to display
 */
function displayCoverLetter(coverLetter) {
    const coverLetterOutput = document.getElementById("coverLetter");
    const coverLetterDiv = document.getElementById("coverLetterDiv");
    coverLetterOutput.innerHTML = coverLetter
    coverLetterDiv.classList.remove("hidden");
}


/**
 * Enables the generate button when the form is changed or input.
 * This ensures that the user can generate a new cover letter after changing the form, preventing duplicate cover letter generation.
 */
function onFormChange() {
    const generateButton = document.getElementById("generateButton");
    generateButton.disabled = false;
}


/**
 * Copies the generated cover letter to the user's clipboard.
 * Called when the copy cover letter button is clicked.
 */
function copyCoverLetter() {
    const coverLetter = document.getElementById("coverLetter").innerText;
    const tooltip = document.getElementById("copyCoverLetterTooltip");
    navigator.clipboard.writeText(coverLetter).then(() => {
        console.log("Copied to clipboard");
        tooltip.classList.remove("opacity-0");
        setTimeout(() => tooltip.classList.add("opacity-0"), 1500);
    }).catch(err => {
        console.error("Failed to copy: ", err);
    });
}


/**
 * When the DOM content is loaded, adds event listeners to the generate button and form inputs.
 */
document.addEventListener("DOMContentLoaded", () => {
    // On click of the generate button, validate the form and generate the cover letter
    const generateButton = document.getElementById("generateButton");
    generateButton.addEventListener("click", validateCoverLetterForm);

    // On form change or input, reset the cover letter generated flag - ensure duplicate cover letters are not generated
    const cvInput = document.getElementById("cv");
    const companyNameInput = document.getElementById("companyName");
    const jobTitleInput = document.getElementById("jobTitle");
    const jobDescriptionInput = document.getElementById("jobDescription");
    cvInput.addEventListener("change", () => onFormChange());
    companyNameInput.addEventListener("input", () => onFormChange());
    jobTitleInput.addEventListener("input", () => onFormChange());
    jobDescriptionInput.addEventListener("input", () => onFormChange());

    // On click for copy cover letter button
    const copyCoverLetterButton = document.getElementById("copyCoverLetterButton");
    copyCoverLetterButton.addEventListener("click", copyCoverLetter);
});