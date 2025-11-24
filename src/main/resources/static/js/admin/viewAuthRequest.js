/**
 * @file viewAuthRequest.js
 * @description JavaScript for view auth request page
 */

/**
 * Updates the action buttons based on the status of the auth request
 * @param authRequestStatus The status of the auth request
 */
function updateActionButtons(authRequestStatus) {
    switch (authRequestStatus) {
        case "PENDING_INITIAL_ADMIN_APPROVAL":
            document.getElementById("finalApprovalButton").disabled = true
            break;
        case "PENDING_FINAL_ADMIN_APPROVAL":
            document.getElementById("initialApprovalButton").disabled = true
            break;
        default:
            document.getElementById("initialApprovalButton").disabled = true
            document.getElementById("finalApprovalButton").disabled = true
            document.getElementById("rejectButton").disabled = true
            break;
    }
}

/**
 * Updates the progress status bar based on the status of the auth request
 * @param authRequestStatus The status of the auth request
 */
function updateProgress(authRequestStatus) {
    switch (authRequestStatus) {
        case "PENDING_INITIAL_ADMIN_APPROVAL":
            document.getElementById("submitted").classList.add("active");
            document.getElementById("lineToInitialApproval").classList.add("active");
            break;
        case "PENDING_COMPANY_APPROVAL":
            document.getElementById("submitted").classList.add("active");
            document.getElementById("lineToInitialApproval").classList.add("active");
            document.getElementById("initialApproval").classList.add("active");
            document.getElementById("lineToCompanyApproval").classList.add("active");
            break;
        case "PENDING_FINAL_ADMIN_APPROVAL":
            document.getElementById("submitted").classList.add("active");
            document.getElementById("lineToInitialApproval").classList.add("active");
            document.getElementById("initialApproval").classList.add("active");
            document.getElementById("lineToCompanyApproval").classList.add("active");
            document.getElementById("companyApproval").classList.add("active");
            document.getElementById("lineToFinalApproval").classList.add("active");
            break;
        case "APPROVED":
            document.getElementById("submitted").classList.add("active");
            document.getElementById("lineToInitialApproval").classList.add("active");
            document.getElementById("initialApproval").classList.add("active");
            document.getElementById("lineToCompanyApproval").classList.add("active");
            document.getElementById("companyApproval").classList.add("active");
            document.getElementById("lineToFinalApproval").classList.add("active");
            document.getElementById("finalApproval").classList.add("active");
            document.getElementById("lineToApproved").classList.add("active");
            document.getElementById("approved").classList.add("active");
            break;
        case "REJECTED_INITIAL_BY_ADMIN":
            document.getElementById("submitted").classList.add("active");
            document.getElementById("lineToInitialApproval").classList.add("rejected");
            document.getElementById("initialApproval").classList.add("rejected");
            break;
        case "REJECTED_BY_COMPANY":
            document.getElementById("submitted").classList.add("active");
            document.getElementById("lineToInitialApproval").classList.add("active");
            document.getElementById("initialApproval").classList.add("active");
            document.getElementById("lineToCompanyApproval").classList.add("rejected");
            document.getElementById("companyApproval").classList.add("rejected");
            break;
        case "REJECTED_FINAL_BY_ADMIN":
            document.getElementById("submitted").classList.add("active");
            document.getElementById("lineToInitialApproval").classList.add("active");
            document.getElementById("initialApproval").classList.add("active");
            document.getElementById("lineToCompanyApproval").classList.add("active");
            document.getElementById("companyApproval").classList.add("active");
            document.getElementById("lineToFinalApproval").classList.add("rejected");
            document.getElementById("finalApproval").classList.add("rejected");
            break;
        default:
            break;
    }
}

/**
 * When the page has loaded, add event listeners to the buttons and update the progress bar
 */
document.addEventListener("DOMContentLoaded", () => {
    // Auth request status - in hidden div
    const authRequestStatus = document.getElementById("authRequestStatus").getAttribute("data-status");

    // Buttons
    const initialApprovalButton = document.getElementById("initialApprovalButton");
    const finalApprovalButton = document.getElementById("finalApprovalButton");
    const rejectButton = document.getElementById("rejectButton");
    const goBackInitialApprovalModal = document.getElementById("goBackInitialApprovalModal");
    const goBackFinalApprovalModal = document.getElementById("goBackFinalApprovalModal");
    const goBackRejectModal = document.getElementById("goBackRejectModal");

    // Modals
    const initialApprovalModal = document.getElementById("initialApprovalModal");
    const finalApprovalModal = document.getElementById("finalApprovalModal");
    const rejectModal = document.getElementById("rejectModal");

    // Event listeners for modal buttons
    initialApprovalButton.addEventListener("click", () => showModal(initialApprovalModal));
    finalApprovalButton.addEventListener("click", () => showModal(finalApprovalModal));
    rejectButton.addEventListener("click", () => showModal(rejectModal));

    // Event listeners for modal back buttons
    goBackInitialApprovalModal.addEventListener("click", () => hideModal(initialApprovalModal));
    goBackFinalApprovalModal.addEventListener("click", () => hideModal(finalApprovalModal));
    goBackRejectModal.addEventListener("click", () => hideModal(rejectModal));

    updateProgress(authRequestStatus)
    updateActionButtons(authRequestStatus)
});