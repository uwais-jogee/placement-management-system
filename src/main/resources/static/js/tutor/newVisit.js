/**
 * @file newVisit.js
 * @description JavaScript for the tutor new visit page.
 */


// Global variables
let autocomplete;
let map;
let directionsService;
let directionsRenderer;
let travelDuration = 0


/**
 * Initialises the Google Maps API - autocomplete, map, and directions services.
 * Called by the Google Maps API.
 */
function initGoogle() {
    const addressInput = document.getElementById("fromAddressInput");
    // Autocomplete initialisation
    autocomplete = new google.maps.places.Autocomplete(
        addressInput,
        {
            types: ["address"],
            componentRestrictions: {country: ["uk"]},
            fields: ["place_id", "formatted_address", "geometry"]
        });
    // Add event listeners for place changed and input events to check the validity of the address
    autocomplete.addListener("place_changed", () => onPlaceChanged(true));
    document.getElementById("fromAddressInput").addEventListener("input", () => onPlaceChanged(false));

    // Map initialisation
    map = new google.maps.Map(document.getElementById("map"), {
        center: {lat: 52.6219263, lng: -1.1271866},
        zoom: 10,
    });

    // Iterate through JSON, extracting the placeId and adding a marker to the map, and info window with company name, address, placement id, and student name
    const placementAddresses = JSON.parse(document.getElementById("hiddenData").getAttribute("data-placement-addresses"));
    for (let i = 0; i < placementAddresses.length; i++) {
        const placeId = placementAddresses[i].placeId;
        const formattedAddress = placementAddresses[i].formattedAddress;
        const companyName = placementAddresses[i].companyName;
        const studentName = placementAddresses[i].studentName;
        const placementId = placementAddresses[i].placementId;

        const service = new google.maps.places.PlacesService(map);
        const infoWindow = new google.maps.InfoWindow();
        service.getDetails({
            placeId: placeId
        }, function (place, status) {
            if (status === google.maps.places.PlacesServiceStatus.OK) {
                const marker = new google.maps.Marker({
                    map: map,
                    position: place.geometry.location,
                    title: companyName,
                    data: {
                        formattedAddress: formattedAddress,
                        placementId: placementId,
                        studentName: studentName
                    }
                });

                // Add a click listener for each marker, and set up the info window.
                marker.addListener("click", () => {
                    infoWindow.close();
                    infoWindow.setContent(`
                            <div>
                                <h2 class="text-lg font-medium text-gray-900">${companyName}</h2>
                                <p class="text-sm text-gray-600">${formattedAddress}</p>
                                <hr>
                                <p class="text-sm text-gray-600">Placement ID: ${placementId}</p>
                                <p class="text-sm text-gray-600">Student: ${studentName}</p>
                            </div>
                        `);
                    infoWindow.open(marker.getMap(), marker);
                });
            }
        });
    }

    // Initialise the Google Maps DirectionsService and DirectionsRenderer
    directionsService = new google.maps.DirectionsService();
    directionsRenderer = new google.maps.DirectionsRenderer();
    directionsRenderer.setMap(map);
}


/**
 * Checks if a valid place has been selected. If valid, removed the custom validity message. If not, sets a custom validity message.
 * @param placeSelected A flag to check if a place has been selected by the user. True if a place has been selected, false if not selected and the user is typing
 */
function onPlaceChanged(placeSelected) {
    const fromAddressInput = document.getElementById("fromAddressInput");
    const place = autocomplete.getPlace();
    if (place && placeSelected === true) {
        console.log("Place found");
        // If a place has been selected, clear the custom validity message
        fromAddressInput.setCustomValidity("");
    } else {
        console.log("Place not found");
        // If a place has not been selected, set a custom validity message
        fromAddressInput.setCustomValidity("Please select an address from the dropdown");
    }
    calculateTravelTime();
}


/**
 * Helper function to format a date object to a string in the format "dd/mm/yy hh:mm"
 * @param date The date object to format
 * @returns {string|string} The formatted date string
 */
function formatDate(date) {
    return date ? date.toLocaleString('en-GB', {
        day: '2-digit',
        month: '2-digit',
        year: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    }).replace(',', '') : '';
}


/**
 * Initialises the FullCalendar library to display the calendar of visits.
 */
function initialiseCalendar() {
    const calendarEl = document.getElementById("calendar");

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: "timeGridWeek",
        locale: 'en-gb',
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
        events: "/tutor/visits/new/calendar-data/get",
        eventColor: "#0d9488",
        eventTextColor: "#ffffff",

        eventMouseEnter: function (mouseEnterInfo) {
            const event = mouseEnterInfo.event;

            const tooltip = document.createElement("div");
            tooltip.className = "fixed bg-white rounded-lg shadow-lg border border-gray-200 p-3";

            tooltip.style.cssText = `
                  position: fixed;
                  z-index: 99999;
                  pointer-events: none;
                  background: white;
                  backdrop-filter: blur(8px);
                `;

            tooltip.innerHTML = `
                    <div class="space-y-1.5">
                        <div class="font-semibold text-gray-800">${event.title}</div>
                        <div class="text-sm text-gray-600">Start: ${formatDate(event.start)}</div>
                        ${event.end ? `<div class="text-sm text-gray-600">End: ${formatDate(event.end)}</div>` : ''}
                        ${event.extendedProps.location ? `<div class="text-sm text-gray-600">Location: ${event.extendedProps.location}</div>` : ''}
                    </div>
            `;

            const rect = mouseEnterInfo.el.getBoundingClientRect();
            tooltip.style.left = rect.right + 10 + "px";
            tooltip.style.top = rect.top + "px";

            document.body.appendChild(tooltip);

            // Keep tooltip in viewport
            setTimeout(() => {
                const tooltipRect = tooltip.getBoundingClientRect();
                if (tooltipRect.right > window.innerWidth) {
                    tooltip.style.left = (rect.left - tooltipRect.width - 10) + "px";
                }
                if (tooltipRect.bottom > window.innerHeight) {
                    tooltip.style.top = (window.innerHeight - tooltipRect.height - 10) + "px";
                }
            }, 0);
        },

        eventMouseLeave: function () {
            const tooltip = document.querySelector(".fixed.bg-white.rounded-lg");
            if (tooltip) {
                tooltip.remove();
            }
        }
    });

    calendar.render();
}


/**
 * Restrict the week picker to the current week and later weeks.
 */
function restrictWeekPicker() {
    const weekPicker = document.getElementById("weekPicker");

    // Get today's date
    const today = new Date();
    const currentYear = today.getFullYear();

    // Calculate the current week number
    const firstDayOfYear = new Date(today.getFullYear(), 0, 1);
    const pastDaysOfYear = Math.floor((today - firstDayOfYear) / 86400000);
    const currentWeek = Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7);

    // Set min value for the week picker to the current week
    weekPicker.min = `${currentYear}-W${String(currentWeek).padStart(2, "0")}`;
}


/**
 * Restrict the date picker to the current week and later weeks.
 */
function restrictDatePicker() {
    const today = new Date();
    const day = String(today.getDate()).padStart(2, "0");
    const month = String(today.getMonth() + 1).padStart(2, "0");
    const year = today.getFullYear();

    // Ensure date picker does not allow dates before today
    const datePicker = document.getElementById("datePicker");
    datePicker.min = `${day}-${month}-${year}`;
}


/**
 * Update the date picker range based on the selected week.
 */
function updateDatePickerRange() {
    const weekPicker = document.getElementById("weekPicker");
    const datePicker = document.getElementById("datePicker");

    if (!weekPicker.value) {
        return;
    }

    // Extract year and week number
    const [year, week] = weekPicker.value.split("-W");

    // Calculate Monday of the selected week
    const firstDayOfYear = new Date(year, 0, 1);
    const dayOffset = firstDayOfYear.getDay() === 0 ? 6 : firstDayOfYear.getDay() - 1; // Adjust to Monday
    const monday = new Date(firstDayOfYear);
    monday.setDate(firstDayOfYear.getDate() - dayOffset + (week - 1) * 7);

    // Get today's date
    const today = new Date();

    let minDate, maxDate;

    if (monday <= today && today <= new Date(monday.getTime() + 6 * 86400000)) {
        // Current week: Start from today and end on Sunday
        minDate = today;
        maxDate = new Date(monday.getTime() + 6 * 86400000); // Sunday of the current week
    } else {
        // Other weeks: Start from Monday and end on Sunday
        minDate = monday;
        maxDate = new Date(monday.getTime() + 6 * 86400000);
    }

    // Format dates as yyyy-mm-dd for the date picker
    const formattedMinDate = `${minDate.getFullYear()}-${String(minDate.getMonth() + 1).padStart(2, "0")}-${String(minDate.getDate()).padStart(2, "0")}`;
    const formattedMaxDate = `${maxDate.getFullYear()}-${String(maxDate.getMonth() + 1).padStart(2, "0")}-${String(maxDate.getDate()).padStart(2, "0")}`;

    // Update date picker
    datePicker.disabled = false;
    datePicker.min = formattedMinDate;
    datePicker.max = formattedMaxDate;
    datePicker.value = "";
}


/**
 * Helper function to get the selected placement ID from the dropdown.
 * @returns {HTMLElement|*}
 */
function getPlacementPlaceId() {
    const selectedPlacement = document.getElementById("placementDropdown").value;
    const placementAddresses = JSON.parse(document.getElementById("hiddenData").getAttribute("data-placement-addresses"));
    for (let i = 0; i < placementAddresses.length; i++) {
        if (placementAddresses[i].placementId == selectedPlacement) {
            return placementAddresses[i].placeId;
        }
    }
}


/**
 * Updates the map to centre on the selected placement.
 */
function showPlacementOnMap() {
    const placeId = getPlacementPlaceId();
    const service = new google.maps.places.PlacesService(map);
    service.getDetails({
        placeId: placeId
    }, function (place, status) {
        if (status === google.maps.places.PlacesServiceStatus.OK) {
            map.setCenter(place.geometry.location);
            map.setZoom(15);
        }
    });
}


/**
 * Calculates the travel time from the tutor's address to the selected placement using Distance Matrix API.
 * Updates the UI with the estimated travel time and displays the route on the map.
 */
function calculateTravelTime() {
    // Check if the form (not including the date and time) is valid before calculating travel time
    const placementDropdown = document.getElementById("placementDropdown");
    const inPersonRadio = document.getElementById("inPersonRadio");
    const fromAddressInput = document.getElementById("fromAddressInput");

    if (!placementDropdown.checkValidity() || !inPersonRadio.checked || !fromAddressInput.checkValidity()) {
        // If the form is not valid, clear the travel time display and map directions
        document.getElementById("travelTimeDisplay").textContent = "Complete the above fields to calculate travel time";
        directionsRenderer.setDirections({routes: []});
        travelDuration = 0;
        return;
    }

    // Get the start and end IDs, and the meeting date and time
    const startPlaceId = autocomplete.getPlace().place_id;
    const endPlaceId = getPlacementPlaceId();

    // Distance Matrix request parameters
    const distanceMatrixRequest = {
        origins: [{placeId: startPlaceId}],
        destinations: [{placeId: endPlaceId}],
        travelMode: google.maps.TravelMode.DRIVING,
    };

    // Use the Distance Matrix API to estimate travel time
    const distanceMatrixService = new google.maps.DistanceMatrixService();
    distanceMatrixService.getDistanceMatrix(distanceMatrixRequest, (response, status) => {
        if (status === google.maps.DistanceMatrixStatus.OK) {
            const result = response.rows[0].elements[0];
            if (result.status === "OK") {
                const travelTimeText = result.duration.text;
                const travelTimeSeconds = result.duration.value;

                console.log("Estimated travel time: " + travelTimeText);

                // Add grace period
                const gracePeriodMinutes = 10;
                const totalTravelTimeSeconds = travelTimeSeconds + (gracePeriodMinutes * 60);
                let totalTravelTimeMinutes = Math.floor(totalTravelTimeSeconds / 60);
                // Round up to the nearest 5 minutes
                console.log("Before rounding: " + totalTravelTimeMinutes)
                totalTravelTimeMinutes = Math.ceil(totalTravelTimeMinutes / 5) * 5;
                console.log("After rounding: " + totalTravelTimeMinutes)

                console.log("Final travel time (rounded +  grace period): " + totalTravelTimeMinutes + " minutes");

                // Update the UI and store the travel duration
                document.getElementById("travelTimeDisplay").textContent = "Estimated travel time (with " + gracePeriodMinutes + " minutes grace): " + totalTravelTimeMinutes + " minutes";
                travelDuration = totalTravelTimeMinutes;

                // Update the map to show the route using Directions API
                const directionsRequest = {
                    origin: {placeId: startPlaceId},
                    destination: {placeId: endPlaceId},
                    travelMode: google.maps.TravelMode.DRIVING,
                };

                directionsService.route(directionsRequest, (result, status) => {
                    if (status === google.maps.DirectionsStatus.OK) {
                        directionsRenderer.setDirections(result);
                    } else {
                        console.error("Directions request failed: ", status);
                    }
                });
            } else {
                console.error("Distance Matrix result failed: ", result.status);
                document.getElementById("travelTimeDisplay").textContent = "Unable to estimate travel time";
                travelDuration = 0;
            }
        } else {
            console.error("Distance Matrix request failed: ", status);
            document.getElementById("travelTimeDisplay").textContent = "Unable to estimate travel time";
            travelDuration = 0;
        }
    });
}


/**
 * When the meeting type is changed, shows/hides the from address input and travel time display.
 * Clears the directions shown on the map.
 */
function onMeetingTypeChanged() {
    const inPersonRadio = document.getElementById("inPersonRadio");
    const fromAddressDiv = document.getElementById("fromAddressDiv");
    const fromAddressInput = document.getElementById("fromAddressInput");
    const travelTimeDiv = document.getElementById("travelTimeDiv");
    const travelTime = document.getElementById("travelTimeDisplay");

    if (inPersonRadio.checked) { // Meeting type In-person
        fromAddressDiv.classList.remove("hidden");
        travelTimeDiv.classList.remove("hidden");
        fromAddressInput.required = true;
    } else { // Meeting type Online
        fromAddressDiv.classList.add("hidden");
        travelTimeDiv.classList.add("hidden");
        fromAddressInput.required = false;
    }

    // Clear the directions shown on the map
    directionsRenderer.setDirections({routes: []});
    travelTime.textContent = "";
    fromAddressInput.value = "";
    travelDuration = 0;
}


/**
 * Displays the suggested visit times in the UI.
 * @param suggestedTimes
 */
function displaySuggestedTimes(suggestedTimes) {
    console.log(suggestedTimes);

    const suggestedTimesDiv = document.getElementById("suggestedTimes");

    // Clear the suggested times div
    suggestedTimesDiv.innerHTML = "";

    if (suggestedTimes.length === 0) {
        // If no suggested times are available, display a message
        const noSuggestedTimesText = document.createElement("p");
        noSuggestedTimesText.textContent = "No suggested visit times found.";
        suggestedTimesDiv.appendChild(noSuggestedTimesText);
    } else {
        // Iterate through the suggested times and create a button for each
        suggestedTimes.forEach((suggestedTime) => {
            // Extract the fields from the suggested time object
            const meetingDuration = suggestedTime.meetingDuration;
            const isOnline = suggestedTime.isOnline;
            const travelDuration = suggestedTime.travelDuration;
            const date = suggestedTime.date;
            const meetingTime = suggestedTime.meetingTime;
            const travelStartTime = suggestedTime.travelStartTime;

            // Format the date and time
            const dateTime = date + "T" + meetingTime;
            const dateObj = new Date(dateTime);
            const formattedDate = dateObj.toLocaleDateString("en-GB");
            const formattedTime = dateObj.toLocaleTimeString("en-GB", {
                hour: "2-digit",
                minute: "2-digit",
                hour12: false,
            });

            // Create the button element
            const suggestedTimeButton = document.createElement("button");
            suggestedTimeButton.type = "button";
            suggestedTimeButton.classList.add("w-full", "flex", "items-center", "justify-between", "px-4", "py-2", "border", "rounded-md", "text-sm", "font-medium", "text-gray-700", "bg-gray-50", "hover:bg-gray-100", "focus:outline-none", "focus:ring-2", "focus:ring-teal-600");

            // Set the date and time span
            const dateTimeSpan = document.createElement("span");
            dateTimeSpan.textContent = formattedDate + ", " + formattedTime;
            suggestedTimeButton.appendChild(dateTimeSpan);

            // Set the duration span, including information about the travel time if the meeting is in-person
            const durationSpan = document.createElement("span");
            if (isOnline) {
                durationSpan.textContent = meetingDuration + " mins";
            } else {
                durationSpan.textContent = meetingDuration + " mins (+ " + travelDuration + " mins travel prior)";
            }
            suggestedTimeButton.appendChild(durationSpan);

            // Add onclick function
            suggestedTimeButton.onclick = () => selectSuggestedTime(dateTime);

            // Append the button to the suggested times div
            suggestedTimesDiv.appendChild(suggestedTimeButton);
        });
    }

    // <button type="button"
    //         className="suggested-time w-full flex items-center justify-between px-4 py-2 border rounded-md text-sm font-medium text-gray-700 bg-gray-50 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-teal-500"
    //         onClick="selectSuggestedTime('2024-06-01T10:00')">
    //     <span>June 1, 10:00 AM</span>
    //     <span>30 mins</span>
    // </button>
}


/**
 * Selects a suggested visit time and sets the date and time pickers to the selected date and time.
 * @param dateTime
 */
function selectSuggestedTime(dateTime) {
    const date = dateTime.split("T")[0];
    const time = dateTime.split("T")[1];

    document.getElementById("datePicker").value = date;
    document.getElementById("timePicker").value = time;
}


/**
 * AJAX request to retrieve suggested visit times based on the selected placement, week, duration, and meeting type.
 * Calls the displaySuggestedTimes function to display the suggested times in the UI.
 */
function getSuggestedTimes() {
    // Check the fields are valid, if not display a message
    const placementDropdown = document.getElementById("placementDropdown");
    const meetingDuration = document.getElementById("duration");
    const meetingType = document.querySelector('input[name="meetingType"]:checked');
    const weekPicker = document.getElementById("weekPicker");
    const fromAddressInput = document.getElementById("fromAddressInput");

    if (!placementDropdown.checkValidity() || !meetingDuration.checkValidity() || !meetingType || !weekPicker.checkValidity() || (meetingType.value === "in-person" && !fromAddressInput.checkValidity())) {
        // Display the feedback messages
        placementDropdown.reportValidity();
        meetingDuration.reportValidity();
        weekPicker.reportValidity();
        if (meetingType.value === "in-person") {
            fromAddressInput.reportValidity();
        }
        return;
    }

    // Determine if the meeting is online or in-person
    const isOnline = meetingType.value !== "in-person";

    // Create request payload - mapped to VisitSuggestionsReqDTO in backend
    const visitSuggestionsReqDTO = {
        placementId: parseInt(placementDropdown.value, 10),
        week: weekPicker.value,
        duration: parseInt(meetingDuration.value, 10),
        isOnline: isOnline,
        travelDuration: travelDuration
    };

    console.log("Request: " + JSON.stringify(visitSuggestionsReqDTO));

    // AJAX request to get suggested visit times
    fetch("/tutor/visits/new/suggested-times/get", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(visitSuggestionsReqDTO)
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error(`Failed to fetch suggested times: ${response.statusText}`);
            }
            return response.json();
        })
        .then((suggestedTimes) => {
            // Display the suggested times
            displaySuggestedTimes(suggestedTimes);
        })
        .catch((error) => {
            console.error("Error fetching suggested times:", error);
        });
}


/**
 * Shows the suggested placements in the UI based on the selected placement and meeting type.
 * Calls the asynchronous function to get nearby placements.
 */
function showSuggestedPlacements() {
    const placementDropdown = document.getElementById("placementDropdown");
    const inPersonRadio = document.getElementById("inPersonRadio");
    const suggestedPlacementsDiv = document.getElementById("suggestedPlacementsDiv");
    const suggestedPlacements = document.getElementById("suggestedPlacements");

    if (!placementDropdown.checkValidity() || !inPersonRadio.checked) {
        // Clear the suggested placements and hide the div if the form is not valid
        suggestedPlacements.innerHTML = "";
        suggestedPlacementsDiv.classList.add("hidden");
        return;
    }

    const selectedPlacementId = placementDropdown.value;
    // Run the asynchronous function to get nearby placements, which will also display the suggested placements
    getNearbyPlacements(selectedPlacementId);
}


/**
 * Gets the nearby placements based on the selected placement, within a 20 mile radius.
 * Uses the Google Distance Matrix API to calculate the distance and duration to each placement.
 * Calls the displaySuggestedPlacements function to display the nearby placements in the UI.'
 * @param selectedPlacementId The ID of the selected placement
 * @returns {*[]} An array of nearby placements
 */
function getNearbyPlacements(selectedPlacementId) {
    const placementAddresses = JSON.parse(document.getElementById("hiddenData").getAttribute("data-placement-addresses"));
    const selectedPlacement = placementAddresses.find(placement => placement.placementId === selectedPlacementId);

    // Check if the selected placement exists
    console.log("Selected Placement: ", selectedPlacement);
    if (!selectedPlacement) {
        console.error("Selected placement not found.");
        return [];
    }

    const distanceMatrixService = new google.maps.DistanceMatrixService();
    const destinations = placementAddresses.map(placement => {
        return {placeId: placement.placeId};
    });
    console.log("Destinations: ", destinations);

    // Request configuration for Google Distance Matrix API
    const distanceMatrixRequest = {
        origins: [{ placeId: selectedPlacement.placeId }], // Selected placement's place id as the origin
        destinations: destinations, // All other placements' place ids as destinations
        travelMode: google.maps.TravelMode.DRIVING,
        unitSystem: google.maps.UnitSystem.IMPERIAL
    };

    // Call Distance Matrix API
    distanceMatrixService.getDistanceMatrix(distanceMatrixRequest, (response, status) => {
        if (status === google.maps.DistanceMatrixStatus.OK) {
            const results = response.rows[0].elements; // Distance and duration data for each destination
            console.log("Distance Matrix API Response: ", response);

            const nearbyPlacements = [];
            placementAddresses.forEach((placement, index) => {
                if (placement.placementId !== selectedPlacement.placementId) {
                    const element = results[index];
                    if (element.status === "OK") {
                        const distance = element.distance.text; // Distance in text format
                        const distanceMetres = element.distance.value; // Distance in meters
                        const duration = element.duration.text; // Duration in text format

                        console.log(`Distance to ${placement.placementId}: ${distance}, ${distanceMetres}, Duration: ${duration}`);

                        // If the distance is less than 20 miles (32186.9 meters), add the placement to the nearbyPlacements array
                        if (distanceMetres <= 32186.9) {
                            nearbyPlacements.push({
                                placementId: placement.placementId,
                                studentName: placement.studentName,
                                companyName: placement.companyName,
                                distance: distance,
                                duration: duration
                            });
                        }
                    } else {
                        console.warn(`No data for destination ${placement.address}`);
                    }
                }
            });
            console.log("Nearby Placements: ", nearbyPlacements);
            displaySuggestedPlacements(nearbyPlacements);
        } else {
            console.error("Distance Matrix API error: ", status);
            return [];
        }
    });
}


/**
 * Displays the suggested placements in the UI.
 * Called after the asynchronous function to get nearby placements has completed.
 * @param nearbyPlacements An array of nearby placements
 */
function displaySuggestedPlacements(nearbyPlacements) {
    const suggestedPlacementsDiv = document.getElementById("suggestedPlacementsDiv");
    const suggestedPlacements = document.getElementById("suggestedPlacements");

    // If there are no nearby placements, hide the div
    if (nearbyPlacements.length === 0) {
        suggestedPlacementsDiv.classList.add("hidden");
        return;
    }

    // Iterate through the nearby placements and create a list item for each
    suggestedPlacements.innerHTML = "";
    nearbyPlacements.forEach((placement) => {
        const listItem = document.createElement("li");
        listItem.textContent = "#" + placement.placementId + " | " + placement.studentName + " | " + placement.companyName + " (" + placement.distance + " - " + placement.duration + ")";
        suggestedPlacements.appendChild(listItem);
    });
    suggestedPlacementsDiv.classList.remove("hidden");

    // <li>#1 | Student Name | Company Name - 20 miles (30 mins)</li>
}


/**
 * Sets the hidden place id and formatted address inputs to autocomplete input value, if the meeting type is in-person.
 * Called when the form is submitted.
 */
function setHiddenInputs() {
    const meetingType = document.querySelector('input[name="meetingType"]:checked');
    const fromPlaceId = document.getElementById("fromPlaceId");
    const fromFormattedAddress = document.getElementById("fromFormattedAddress");

    if (meetingType.value === "in-person") {
        // Set the hidden inputs to the selected autocomplete address
        fromPlaceId.value = autocomplete.getPlace().place_id;
        fromFormattedAddress.value = autocomplete.getPlace().formatted_address;
        // Set the hidden travel duration input to the globally set travel duration
        document.getElementById("travelDuration").value = travelDuration;
    } else {
        // Clear the hidden inputs if the meeting type is online
        fromPlaceId.value = "";
        fromFormattedAddress.value = "";
        // Set the hidden travel duration input to 0
        document.getElementById("travelDuration").value = 0;

    }
}


/**
 * Validates the form before showing the confirmation modal
 */
function validateForm() {
    const confirmModal = document.getElementById("confirmModal");
    const visitForm = document.getElementById("visitForm");

    // Check if the form is valid before showing the modal
    if (visitForm.checkValidity()) {
        showModal(confirmModal);
    } else {
        // Show the UI validation messages if the form is not valid
        visitForm.reportValidity();
    }
}


/**
 * Sets the hidden inputs and submits the form.
 * Called when the user confirms the visit in the modal.
 */
function submitForm() {
    setHiddenInputs();
    document.getElementById("visitForm").submit();
}


/**
 * When the document loads, adds event listeners to buttons and form fields.
 * Calls restrict date/week picker functions
 */
document.addEventListener("DOMContentLoaded", function () {
    const hasLinkedCalendar = document.getElementById("calendar").getAttribute("data-has-linked-calendar");

    const weekPicker = document.getElementById("weekPicker");
    const datePicker = document.getElementById("datePicker");

    // Disable date picker initially, and restrict both pickers
    datePicker.disabled = true;
    restrictWeekPicker();
    restrictDatePicker();

    // Event listener for week selection
    weekPicker.addEventListener("change", updateDatePickerRange);

    // On Change listener for the placement picker to update the map, so it centres on the selected placement
    document.getElementById("placementDropdown").addEventListener("change", showPlacementOnMap);

    // On change listener for the meeting type radio buttons
    document.getElementById("onlineRadio").addEventListener("change", onMeetingTypeChanged);
    document.getElementById("inPersonRadio").addEventListener("change", onMeetingTypeChanged);

    // On change listener for form fields to calculate travel time
    document.getElementById("placementDropdown").addEventListener("change", calculateTravelTime);
    document.getElementById("duration").addEventListener("change", calculateTravelTime);
    document.getElementById("onlineRadio").addEventListener("change", calculateTravelTime);
    document.getElementById("inPersonRadio").addEventListener("change", calculateTravelTime);
    document.getElementById("weekPicker").addEventListener("change", calculateTravelTime);
    document.getElementById("datePicker").addEventListener("change", calculateTravelTime);
    document.getElementById("timePicker").addEventListener("change", calculateTravelTime);
    // document.getElementById("fromAddressInput").addEventListener("change", calculateTravelTime);

    // On change listener for the select placements and meeting type, to show other nearby suggested placement visits
    document.getElementById("placementDropdown").addEventListener("change", showSuggestedPlacements);
    document.getElementById("onlineRadio").addEventListener("change", showSuggestedPlacements);
    document.getElementById("inPersonRadio").addEventListener("change", showSuggestedPlacements);

    if (hasLinkedCalendar === "true") {
        document.getElementById("syncButton").classList.add("hidden");
        document.getElementById("calendar").classList.remove("hidden");
        document.getElementById("suggestedTimesSyncPrompt").classList.add("hidden");
        document.getElementById("suggestTimesButtonDiv").classList.remove("hidden");
        initialiseCalendar();
    } else {
        document.getElementById("calendar").classList.add("hidden");
        document.getElementById("syncButton").classList.remove("hidden");
        document.getElementById("suggestTimesButtonDiv").classList.add("hidden");
        document.getElementById("suggestedTimesSyncPrompt").classList.remove("hidden");
    }

    // On click listener for the get suggested times button
    document.getElementById("suggestTimesButton").addEventListener("click", getSuggestedTimes);

    // On click listener for the submit button, to validate the form and show the modal
    document.getElementById("submitButton").addEventListener("click", validateForm);

    // On click listener for the confirm button in the modal, to submit the form
    document.getElementById("confirmSubmitButton").addEventListener("click", submitForm);

    // On click listener for the back button in the modal, to hide the modal
    const confirmModal = document.getElementById("confirmModal");
    document.getElementById("goBackConfirmModalButton").addEventListener("click", () => hideModal(confirmModal));
});