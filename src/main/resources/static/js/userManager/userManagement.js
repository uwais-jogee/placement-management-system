/**
 * @file userManagement.js
 * @description JavaScript for the user management page
 */


/**
 * Shows the filter box, called when the filter button is clicked
 */
function showFilterBox() {
    const filterBox = document.getElementById("filterBox");
    filterBox.classList.remove("hidden");
}


/**
 * Hides the filter box, called when clicking outside the filter box
 * @param event The click event
 */
function hideFilterBox(event) {
    const filterButton = document.getElementById("filterButton");
    const filterBox = document.getElementById("filterBox");
    // Check if the click target is not the filter box or the button, then close the filter box
    if (!filterBox.classList.contains("hidden") && !filterBox.contains(event.target) && !filterButton.contains(event.target)) {
        filterBox.classList.add("hidden");
    }
}


/**
 * Filters the table based on the status and active filter checkboxes that are checked
 */
function filterTable() {
    const activeFilterCheckbox = document.getElementById("activeFilterCheckbox");
    const inactiveFilterCheckbox = document.getElementById("inactiveFilterCheckbox");
    const enabledFilterCheckbox = document.getElementById("enabledFilterCheckbox");
    const disabledFilterCheckbox = document.getElementById("disabledFilterCheckbox");

    // Check which checkboxes are checked
    const active = activeFilterCheckbox.checked;
    const inactive = inactiveFilterCheckbox.checked;
    const enabled = enabledFilterCheckbox.checked;
    const disabled = disabledFilterCheckbox.checked;

    // Construct the filter conditions
    let filters = [];

    if (active && enabled) filters.push("True True");
    if (active && enabled) filters.push("True False");
    if (inactive && enabled) filters.push("False True");
    if (inactive && disabled) filters.push("False False");
    // If no filters are selected, show no rows
    let searchRegex = filters.length > 0 ? `^(${filters.join("|")})$` : "$^";

    // Perform the search on the hidden combined status column
    const filterStatusColIndex = 8; // Column index for the hidden combined status column
    const dataTable = $("#userTable").DataTable(); // jQuery to get the DataTable instance
    dataTable.column(filterStatusColIndex).search(searchRegex, true, false).draw();
}


/**
 * AJAX request to check if the username is available.
 * Displays the error message if the username is not available, and updates the submit button.
 */
function validateUsername() {
    const username = document.getElementById("username");
    const usernameError = document.getElementById("usernameError");

    // AJAX request to check if the username is available
    fetch("/user-manager/user/new/validate-username?username=" + username.value, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
    })
    .then(response => response.json())
    .then(data => {
        if (data.available) {
            // Hide the error message
            usernameError.classList.add("hidden");
            // Change ring color back to gray
            username.classList.remove("ring-red-300");
            username.classList.add("ring-gray-300");
        } else {
            // Show the error message
            usernameError.classList.remove("hidden");
            // Change ring color to red
            username.classList.remove("ring-gray-300");
            username.classList.add("ring-red-300");
        }
        updateSubmitButton();
    })
    .catch(error => console.error("Error checking username availability:", error));
}


/**
 * AJAX request to check if the email is available.
 * Displays the error message if the username is not available, and updates the submit button.
 */
function validateEmail() {
    const email = document.getElementById("email");
    const emailError = document.getElementById("emailError");
    const submitCreateUserModal = document.getElementById("submitCreateUserModal");

    // AJAX request to check if the username is available
    fetch("/user-manager/user/new/validate-email?email=" + email.value, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
    })
        .then(response => response.json())
        .then(data => {
            if (data.available) {
                // Hide the error message
                emailError.classList.add("hidden");
                // Change ring color back to gray
                email.classList.remove("ring-red-300");
                email.classList.add("ring-gray-300");
            } else {
                // Show the error message
                emailError.classList.remove("hidden");
                // Change ring color to red
                email.classList.remove("ring-gray-300");
                email.classList.add("ring-red-300");
            }
            updateSubmitButton();
        })
        .catch(error => console.error("Error checking email availability:", error));
}


function updateSubmitButton() {
    const usernameError = document.getElementById("usernameError");
    const emailError = document.getElementById("emailError");
    const submitCreateUserModal = document.getElementById("submitCreateUserModal");

    if (usernameError.classList.contains("hidden") && emailError.classList.contains("hidden")) {
        submitCreateUserModal.disabled = false;
    } else {
        submitCreateUserModal.disabled = true;
    }
}


function cancelCreateUser() {
    const createUserModal = document.getElementById("createUserModal");
    const createUserForm = document.getElementById("createUserForm");
    const usernameError = document.getElementById("usernameError");
    const username = document.getElementById("username");

    // Reset and hide the modal
    hideModal(createUserModal);
    createUserForm.reset();
    usernameError.classList.add("hidden");
    username.classList.remove("ring-red-300");
    username.classList.add("ring-gray-300");
}


/**
 * On document load, adds event listeners for the filter button, filter checkboxes, and clicking outside the filter box.
 * Create event listeners for the create user modal inputs and buttons.
 */
document.addEventListener("DOMContentLoaded", function () {
    const filterButton = document.getElementById("filterButton");

    // On click listener for the filter button to show the filter box
    filterButton.addEventListener("click", showFilterBox);

    // On click listener to close the filter box when clicking elsewhere
    document.addEventListener("click", hideFilterBox);

    // On click listeners for the filter checkboxes, to filter the table
    const enabledFilterCheckbox = document.getElementById("enabledFilterCheckbox");
    enabledFilterCheckbox.addEventListener("click", filterTable);
    const disabledFilterCheckbox = document.getElementById("disabledFilterCheckbox");
    disabledFilterCheckbox.addEventListener("click", filterTable);
    const activeFilterCheckbox = document.getElementById("activeFilterCheckbox");
    activeFilterCheckbox.addEventListener("click", filterTable);
    const inactiveFilterCheckbox = document.getElementById("inactiveFilterCheckbox");
    inactiveFilterCheckbox.addEventListener("click", filterTable);

    // Hide the combined status column used for filtering
    const dataTable = $("#userTable").DataTable();
    dataTable.column(8).visible(false);

    // Event listener for the Create New User button to show the create user modal
    const createUserButton = document.getElementById("createUserButton");
    const createUserModal = document.getElementById("createUserModal");
    createUserButton.addEventListener("click", () => showModal(createUserModal));

    // Event listener for the Cancel button on the create user modal
    const cancelCreateUserModal = document.getElementById("cancelCreateUserModal");
    cancelCreateUserModal.addEventListener("click", cancelCreateUser);

    // On input listener for the username field to validate the username is available
    const usernameInput = document.getElementById("username");
    usernameInput.addEventListener("input", validateUsername);

    // On input listener for the email field to validate the email is available
    const emailInput = document.getElementById("email");
    emailInput.addEventListener("input", validateEmail);
});