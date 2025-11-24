/**
 * @file viewAuthRequest.js
 * @description JavaScript for the company view auth request page
 */

/**
 * When the document is loaded, adds event listeners to the approval and reject buttons, and modal back buttons
 */
document.addEventListener("DOMContentLoaded", () => {
    // Buttons
    const approveButton = document.getElementById("approveButton");
    const rejectButton = document.getElementById("rejectButton");
    const goBackApproveModal = document.getElementById("goBackApproveModal");
    const goBackRejectModal = document.getElementById("goBackRejectModal");

    // Modals
    const approveModal = document.getElementById("approveModal");
    const rejectModal = document.getElementById("rejectModal");

    // Event listeners
    approveButton.addEventListener("click", () => showModal(approveModal));
    rejectButton.addEventListener("click", () => showModal(rejectModal));
    goBackApproveModal.addEventListener("click", () => hideModal(approveModal));
    goBackRejectModal.addEventListener("click", () => hideModal(rejectModal));
});