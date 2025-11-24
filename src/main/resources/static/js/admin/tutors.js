/**
 * @file tutors.js
 * @description JavaScript for the tutors page
 */

/**
 * Populates the placements modal table with the given placements data from the AJAX request
 * @param placements The placements data to populate the table with
 * @param tutorName The name of the tutor to display in the modal title
 */
function populatePlacementsTable(placements, tutorName) {
    const tableBody = document.getElementById("placementsModalTableBody");
    const tableInfo = document.getElementById("placementsModalTableInfo");

    // Clear the table body
    tableBody.innerHTML = "";

    // Update the modal title with the tutor's name
    const modalTitle = document.getElementById("placementsModalTitle");
    modalTitle.textContent = "Placements: " + tutorName;

    // Check if there are any placements
    if (placements.length === 0) {
        // If there are no placements, display a message in the table info
        tableInfo.textContent = "No records found";
        return;
    }

    // Update the table info with the number of placements
    tableInfo.textContent = "Showing 0 to " + placements.length + " of " + placements.length + " entries";

    // Populate the table with the placements data
    placements.forEach(placement => {
        const row = document.createElement("tr");
        row.classList.add("hover:bg-gray-50");

        // ID
        const idCell = document.createElement("td");
        idCell.id = "placementId";
        idCell.classList.add("px-4", "py-3", "text-sm", "text-gray-900", "text-center");
        idCell.textContent = "#" + placement.placementId;
        row.appendChild(idCell);

        // Student Name
        const studentNameCell = document.createElement("td");
        studentNameCell.id = "studentName";
        studentNameCell.classList.add("px-4", "py-3", "text-sm", "text-gray-900");
        studentNameCell.textContent = placement.studentName;
        row.appendChild(studentNameCell);

        // Company Name
        const companyNameCell = document.createElement("td");
        companyNameCell.id = "companyName";
        companyNameCell.classList.add("px-4", "py-3", "text-sm", "text-gray-900");
        companyNameCell.textContent = placement.companyName;
        row.appendChild(companyNameCell);

        // Start Date
        const startDateCell = document.createElement("td");
        startDateCell.id = "startDate";
        startDateCell.classList.add("px-4", "py-3", "text-sm", "text-gray-900");
        startDateCell.textContent = placement.startDate;
        row.appendChild(startDateCell);

        // End Date
        const endDateCell = document.createElement("td");
        endDateCell.id = "endDate";
        endDateCell.classList.add("px-4", "py-3", "text-sm", "text-gray-900");
        endDateCell.textContent = placement.endDate;
        row.appendChild(endDateCell);

        // Status
        const statusCell = document.createElement("td");
        statusCell.id = "status";
        statusCell.classList.add("text-sm", "text-gray-900", "text-center");
        const statusSpan = document.createElement("span");
        statusSpan.classList.add("text-nowrap", "px-2", "py-1", "text-sm", "font-medium", "rounded-full");
        statusSpan.setAttribute("data-status", placement.status);
        statusSpan.textContent = placement.status;

        // Add status-specific styles
        if (placement.status === "In Progress") {
            statusSpan.classList.add("bg-green-100", "text-green-800");
        } else if (placement.status === "Upcoming") {
            statusSpan.classList.add("bg-blue-100", "text-blue-800");
        } else if (placement.status === "Pending Student Evaluation") {
            statusSpan.classList.add("bg-yellow-100", "text-yellow-800");
        } else if (placement.status === "Completed") {
            statusSpan.classList.add("bg-gray-100", "text-gray-800");
        }

        statusCell.appendChild(statusSpan);
        row.appendChild(statusCell);

        // View
        const viewCell = document.createElement("td");
        viewCell.classList.add("px-4", "py-3", "text-sm", "text-gray-900", "text-center");
        const viewLink = document.createElement("a");
        viewLink.id = "viewPlacementLink";
        viewLink.href = "/admin/placement/view?id=" + placement.placementId;
        const viewIcon = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        viewIcon.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        viewIcon.setAttribute("fill", "none");
        viewIcon.setAttribute("viewBox", "0 0 24 24");
        viewIcon.setAttribute("stroke-width", "1.5");
        viewIcon.setAttribute("stroke", "currentColor");
        viewIcon.classList.add("h-6", "w-6", "text-teal-600", "hover:text-teal-400", "inline-block");

        const viewPath = document.createElementNS("http://www.w3.org/2000/svg", "path");
        viewPath.setAttribute("stroke-linecap", "round");
        viewPath.setAttribute("stroke-linejoin", "round");
        viewPath.setAttribute("d", "M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3");

        viewIcon.appendChild(viewPath);
        viewLink.appendChild(viewIcon);
        viewCell.appendChild(viewLink);
        row.appendChild(viewCell);

        // Append row to table body
        tableBody.appendChild(row);
    });
}

/**
 * Fetches the tutor's placements data from the server with an AJAX request and calls populatePlacementsTable to display it
 * @param buttonEl The button element that was clicked to view the placements - contains the tutor's username used to fetch the data, and name to display in the modal title
 */
function fetchTutorPlacements(buttonEl) {
    // Get the tutor ID from the data attribute
    const tutorUsername = buttonEl.getAttribute("data-tutor-username");
    const tutorName = buttonEl.getAttribute("data-tutor-name");

    // AJAX request to fetch the tutor's placements
    fetch("/admin/tutors/placements?username=" + tutorUsername, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        }
    })
        .then(response => response.json())
        .then(data => {
            populatePlacementsTable(data, tutorName);
        })
        .catch(error => console.error(error));
}

/**
 * Event listener for the DOMContentLoaded event - adds event listeners to the view buttons and modal close button
 */
document.addEventListener("DOMContentLoaded", () => {
    const placementsModal = document.getElementById("placementsModal");

    // Add event listener for each view button to populate the placements table then show the modal
    const viewButtons = document.querySelectorAll(".viewPlacementsButton");
    for (const buttonEl of viewButtons) {
        buttonEl.addEventListener("click", () => fetchTutorPlacements(buttonEl));
        buttonEl.addEventListener("click", () => showModal(placementsModal));
    }

    // Add event listener for the modal close button
    const modalCloseButton = document.getElementById("placementsModalCloseButton");
    modalCloseButton.addEventListener("click", () => hideModal(placementsModal));
});