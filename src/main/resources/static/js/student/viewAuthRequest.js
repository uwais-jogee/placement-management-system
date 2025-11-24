/**
 * @file viewAuthRequest.js
 * @description JavaScript for student view auth request page
 */


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
 * When the page has loaded, add update the progress bar with the request status
 */
document.addEventListener("DOMContentLoaded", () => {
    // Auth request status - in hidden div
    const authRequestStatus = document.getElementById("authRequestStatus").getAttribute("data-status");
    updateProgress(authRequestStatus)
});