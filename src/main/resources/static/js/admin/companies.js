/**
 * @file companies.js
 * @description JavaScript for the companies page
 */

// Global variables
let topCompaniesChart;
let map;
let autocomplete;

/**
 * Populates the placements modal table with the given placements data from the AJAX request
 * @param placements The placements data to populate the table with
 * @param companyName The name of the company to display in the modal title
 */
function populatePlacementsTable(placements, companyName) {
    const tableBody = document.getElementById("placementsModalTableBody");
    const tableInfo = document.getElementById("placementsModalTableInfo");

    // Clear the table body
    tableBody.innerHTML = "";

    // Update the modal title with the tutor's name
    const modalTitle = document.getElementById("placementsModalTitle");
    modalTitle.textContent = "Placements: " + companyName;

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
 * Fetches the company's placements data from the server and populates the placements modal table
 * @param buttonEl The button element that was clicked to view the placements - contains the company's ID used to fetch the data, and name to display in the modal title
 */
function fetchCompanyPlacements(buttonEl) {
    // Get the company ID from the data attribute
    const companyId = buttonEl.getAttribute("data-company-id");
    const companyName = buttonEl.getAttribute("data-company-name");

    // AJAX request to fetch the company's placements
    fetch("/admin/companies/placements?id=" + companyId, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        }
    })
        .then(response => response.json())
        .then(data => {
            populatePlacementsTable(data, companyName);
        })
        .catch(error => console.error(error));
}

/**
 * Initialises the top companies chart with no data - ran on page load
 */
function initChart() {
    topCompaniesChart = new Chart("topCompaniesChart", {
        type: "bar",
        options: {
            maintainAspectRatio: false,
            responsive: true,
            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: true,
                    color: "#111827"
                }
            },
            scales: {
                y: {
                    ticks: {
                        callback: function (value) {
                            return Number.isInteger(value) ? value : ''; // Show only whole numbers
                        }
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        color: "#111827"
                    }
                }
            }
        }
    });
}

/**
 * Filters the top companies chart by the selected category, fetching the relevant data and updating the chart
 * @param category The category to filter the chart by
 * @param activeButton The button element that was clicked to filter the chart
 */
function filterChart(category, activeButton) {
    // Add active styling to the clicked button
    const buttons = document.querySelectorAll(".filterButtons");
    for (const btn of buttons) {
        // Remove active styling from all buttons
        btn.classList.remove("bg-teal-600", "border-teal-600", "text-white", "hover:bg-teal-500");
        // Add normal styling to all buttons
        btn.classList.add("border-gray-200", "bg-gray-100", "text-gray-600", "hover:bg-gray-50");
    }
    // Set active styling to the clicked button
    activeButton.classList.add("bg-teal-600", "border-teal-600", "text-white", "hover:bg-teal-500");
    // Remove normal styling from the clicked button
    activeButton.classList.remove("border-gray-200", "bg-gray-100", "text-gray-600", "hover:bg-gray-50");

    // Get the chart data from the AJAX endpoint and update the chart
    fetch("/admin/companies/top-companies-chart/get?category=" + category, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        }
    })
        .then(response => response.json())
        .then(data => {
            // Update the chart with the new data
            // Add background color to the data
            data.datasets[0].backgroundColor = generateChartColours(data.labels.length);
            topCompaniesChart.data = data;
            topCompaniesChart.options.plugins.title.text = "Top Companies by " + activeButton.textContent + " Rating"
            topCompaniesChart.update();
        })
        .catch(error => console.error(error));
}


/**
 * Initialises the Google Maps API autocomplete and map
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

    // Map initialisation
    map = new google.maps.Map(document.getElementById("map"), {
        center: {lat: 52.6219263, lng: -1.1271866},
        zoom: 10,
    });

    // Iterate through JSON, extracting the placeId and adding a marker to the map, and info window with company name, address, placement id, and student name
    const companyAddresses = JSON.parse(document.getElementById("map").getAttribute("data-company-addresses"));
    for (let i = 0; i < companyAddresses.length; i++) {
        const companyName = companyAddresses[i].companyName;
        const formattedAddress = companyAddresses[i].formattedAddress;
        const placeId = companyAddresses[i].placeId;

        const service = new google.maps.places.PlacesService(map);
        const infoWindow = new google.maps.InfoWindow();
        service.getDetails({placeId: placeId}, function (place, status) {
            if (status === google.maps.places.PlacesServiceStatus.OK) {
                const marker = new google.maps.Marker({
                    map: map,
                    position: place.geometry.location,
                    title: companyName,
                    data: {
                        formattedAddress: formattedAddress,
                    }
                });

                // Add a click listener for each marker, and set up the info window.
                marker.addListener("click", () => {
                    infoWindow.close();
                    infoWindow.setContent(`
                            <div>
                                <h2 class="text-lg font-medium text-gray-900">${companyName}</h2>
                                <p class="text-sm text-gray-600">${formattedAddress}</p>
                            </div>
                        `);
                    infoWindow.open(marker.getMap(), marker);
                });
            }
        });
    }
}


/**
 * Checks if a valid place has been selected. If valid, sets the hidden inputs to the place. If not, sets a custom validity message.
 * @param placeSelected A flag to check if a place has been selected by the user. True if a place has been selected, false if not selected and the user is typing
 */
function onPlaceChanged(placeSelected) {
    const companyAddressInput = document.getElementById("companyAddress");
    const place = autocomplete.getPlace();
    if (place && placeSelected === true) {
        console.log("Place found");
        // If a place has been selected, clear the custom validity message and set the hidden inputs to the place
        companyAddressInput.setCustomValidity("");
        document.getElementById("companyPlaceId").value = autocomplete.getPlace().place_id;
        document.getElementById("companyFormattedAddress").value = autocomplete.getPlace().formatted_address;
    } else {
        console.log("Place not found");
        // If a place has not been selected, set a custom validity message and clear the hidden inputs
        companyAddressInput.setCustomValidity("Please select an address from the dropdown");
        document.getElementById("companyPlaceId").value = "";
        document.getElementById("companyFormattedAddress").value = "";
    }
}

/**
 * When the page has loaded, adds the event listeners for the view buttons, modal close button, create company button, and filter buttons. Runs the chart initialisation and filtering functions
 */
document.addEventListener("DOMContentLoaded", () => {
    const placementsModal = document.getElementById("placementsModal");

    // Add event listener for each view button to populate the placements table then show the modal
    const viewButtons = document.querySelectorAll(".viewPlacementsButton");
    for (const buttonEl of viewButtons) {
        buttonEl.addEventListener("click", () => fetchCompanyPlacements(buttonEl));
        buttonEl.addEventListener("click", () => showModal(placementsModal));
    }

    // Add event listener for the modal close button
    const modalCloseButton = document.getElementById("placementsModalCloseButton");
    modalCloseButton.addEventListener("click", () => hideModal(placementsModal));

    // Add event listeners for the create company modal
    const createCompanyModal = document.getElementById("createCompanyModal");
    const createCompanyButton = document.getElementById("createCompanyButton");
    const createCompanyModalCancelButton = document.getElementById("createCompanyModalCancelButton");
    const createCompanyForm = document.getElementById("createCompanyForm");
    createCompanyButton.addEventListener("click", () => showModal(createCompanyModal));
    createCompanyModalCancelButton.addEventListener("click", () => {
        createCompanyForm.reset();
        hideModal(createCompanyModal);
    });

    // Add event listeners to the filter chart buttons
    const filterButtons = document.querySelectorAll(".filterButtons");
    for (const button of filterButtons) {
        button.addEventListener("click", () => filterChart(button.getAttribute("data-category"), button));
    }

    // Initialise the chart, filter by the overall rating category
    initChart();
    filterChart("overall_rating", filterButtons[0]);

});