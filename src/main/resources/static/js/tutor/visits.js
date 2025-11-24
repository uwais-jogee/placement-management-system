/**
 * @file visits.js
 * @description Javascript for the tutor visits page
 */


/**
 * Shows the visit details in the details area.
 * Called when a visit is clicked in the table/calendar.
 * @param tableRow The table row that contains the visit details
 */
function showVisitDetails(tableRow) {
    const visitDetailsPrompt = document.getElementById("visitDetailsPrompt");
    visitDetailsPrompt.classList.add("hidden"); // Hide the prompt

    const visitDetailsArea = document.getElementById("visitDetailsArea");
    visitDetailsArea.classList.remove("hidden"); // Show the details area

    // Dynamically set the visit details based on the table row clicked, getting the text from each cell
    document.getElementById("visitId").innerText = tableRow["id"];
    document.getElementById("studentName").innerText = tableRow.cells[0].innerText;
    document.getElementById("companyName").innerText = tableRow.cells[1].innerText;
    document.getElementById("status").innerText = tableRow.cells[2].textContent.trim();
    document.getElementById("date").innerText = tableRow.cells[3].innerText;
    document.getElementById("startEndTime").innerText = tableRow.cells[4].innerText;
    document.getElementById("location").innerText = tableRow.cells[5].innerText;
    document.getElementById("travelDuration").innerText = tableRow.cells[6].innerText;

    const statusSpan = tableRow.cells[2].querySelector("span");
    const status = statusSpan ? statusSpan.textContent.trim() : "";

    // Do not display the cancel button if the visit is not state 'Upcoming'
    const cancelVisitButton = document.getElementById("cancelVisitButton");
    if (status !== "Upcoming") {
        cancelVisitButton.classList.add("hidden");
        // Update the hidden input in the cancel visit form to be null as the visit cannot be cancelled
        document.getElementById("cancelVisitId").value = null;
    } else {
        cancelVisitButton.classList.remove("hidden");
        // Update the hidden input in the cancel visit form with the visit ID
        document.getElementById("cancelVisitId").value = tableRow["id"];
    }

    const statusSpanDetails = document.getElementById("status");
    // Remove any existing classes
    statusSpanDetails.classList.remove("bg-green-100", "text-green-800", "bg-yellow-100", "text-yellow-800", "bg-red-100", "text-red-800");
    // Set the visit status classes for the details area
    if (status === "Completed") {
        statusSpanDetails.classList.add("bg-green-100", "text-green-800");
    } else if (status === "Upcoming") {
        statusSpanDetails.classList.add("bg-yellow-100", "text-yellow-800");
    } else if (status === "Cancelled") {
        statusSpanDetails.classList.add("bg-red-100", "text-red-800");
    }
}


/**
 * Highlights the clicked row in the table
 * @param tableRow The row that was clicked
 */
function highlightRow(tableRow) {
    // Remove the selected class from all rows
    const rows = document.querySelectorAll(".visitRow");
    rows.forEach(row => row.classList.remove("bg-gray-50"));

    // Add the selected class to the clicked row
    tableRow.classList.add("bg-gray-50");
}


/**
 * Highlights the clicked event in the calendar
 * @param event The calendar event that was clicked
 */
function highlightEvent(event) {
    // Remove the selected class from all events
    const events = document.querySelectorAll(".fc-event");
    events.forEach(event => event.classList.remove("bg-gray-200"));

    // Add the selected class to the clicked event
    event.classList.add("bg-gray-200");
}


/**
 * Toggles the view between the list and calendar view of visits.
 * Called when the list or calendar view button is clicked.
 * @param selected The name of the view that has been selected
 */
function toggleView(selected) {
    // Get the buttons
    const listToggle = document.getElementById("listToggle");
    const calendarToggle = document.getElementById("calendarToggle");
    const listView = document.getElementById("listView");
    const calendarView = document.getElementById("calendarView");
    const visitDetailsPrompt = document.getElementById("visitDetailsPrompt");
    const visitDetailsArea = document.getElementById("visitDetailsArea");

    // Hide the visit details area and show the prompt
    visitDetailsArea.classList.add("hidden");
    visitDetailsPrompt.classList.remove("hidden");

    if (selected === "list") {
        // Set List as active
        listToggle.classList.add("bg-teal-600", "text-white", "hover:bg-teal-500");
        listToggle.classList.remove("bg-gray-200", "text-gray-800", "hover:bg-gray-300");
        // Set Calendar as inactive
        calendarToggle.classList.add("bg-gray-200", "text-gray-800", "hover:bg-gray-300");
        calendarToggle.classList.remove("bg-teal-600", "text-white", "hover:bg-teal-500");

        // Show the list view and hide the calendar view
        listView.classList.remove("hidden");
        calendarView.classList.add("hidden");

        // Remove the selected class from all rows
        const rows = document.querySelectorAll("tbody tr");
        rows.forEach(row => row.classList.remove("bg-gray-50"));
    } else {
        // Set Calendar as active
        calendarToggle.classList.add("bg-teal-600", "text-white", "hover:bg-teal-500");
        calendarToggle.classList.remove("bg-gray-200", "text-gray-800", "hover:bg-gray-300");
        // Set List as inactive
        listToggle.classList.add("bg-gray-200", "text-gray-800", "hover:bg-gray-300");
        listToggle.classList.remove("bg-teal-600", "text-white", "hover:bg-teal-500");

        // Hide the list view and show the calendar view
        listView.classList.add("hidden");
        calendarView.classList.remove("hidden");

        // Initialise the calendar
        initialiseCalendar();
    }
}


/**
 * Initialises the FullCalendar plugin to display the visits in a calendar view.
 */
function initialiseCalendar() {
    const calendarEl = document.getElementById("calendar");
    const visitsJson = calendarEl.getAttribute("data-visitsJson");

    // Parse the visits JSON before passing to the calendar
    const events = JSON.parse(visitsJson);

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: "dayGridMonth",
        locale: "en-gb",
        headerToolbar: {
            left: "title",
            center: "",
            right: "customToday prev,next",
        },
        customButtons: {
            customToday: {
                text: "Today",
                click: function () {
                    calendar.today();
                }
            }
        },
        events: events,
        eventColor: '#0d9488',
        eventTextColor: '#ffffff',
        eventTimeFormat: { // Custom time format for events
            hour: '2-digit',
            minute: '2-digit',
            hour12: false // Use 24-hour format
        },
        eventClick: function (info) {
            // Show the visit details when an event is clicked

            // Get the visit ID from the event
            const visitId = info.event.id;
            // Find the corresponding visit from the list view
            const visitRow = document.getElementById(visitId);
            // Show the visit details
            showVisitDetails(visitRow);
            // Highlight the event
            highlightEvent(info.el);
        }
    });

    calendar.render();
}


/**
 * When the document loads, adds event listeners to the view toggles, table rows, modal, and buttons
 */
document.addEventListener("DOMContentLoaded", () => {
    // Event listeners for the view toggles
    const listToggle = document.getElementById("listToggle");
    const calendarToggle = document.getElementById("calendarToggle");
    listToggle.addEventListener("click", () => toggleView("list"));
    calendarToggle.addEventListener("click", () => toggleView("calendar"));

    // Event listener for the table rows
    const visitRows = document.querySelectorAll(".visitRow");
    for (const row of visitRows) {
        row.addEventListener("click", () => {
            showVisitDetails(row);
            highlightRow(row);
        });
    }

    // Event listener for cancel visit button
    const cancelVisitButton = document.getElementById("cancelVisitButton");
    const cancelVisitModal = document.getElementById("cancelVisitModal");
    cancelVisitButton.addEventListener("click", () => showModal(cancelVisitModal));

    // Event listener for the back button in the cancel visit modal
    const goBackCancelVisitModal = document.getElementById("goBackCancelVisitModal");
    goBackCancelVisitModal.addEventListener("click", () => hideModal(cancelVisitModal));
});