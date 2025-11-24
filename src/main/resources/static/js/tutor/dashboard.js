/**
 * @file dashboard.js
 * @description JavaScript for the tutor dashboard page
 */


// Global variables
let map;


/**
 * Acknowledge a notification by sending an AJAX request to the server and updating the UI
 * @param checkboxInput The checkbox input element of the notification that has been clicked
 */

function acknowledgeNotification(checkboxInput) {
    const notificationId = checkboxInput.getAttribute("data-notification-id");
    const notificationElement = document.getElementById("notification-" + notificationId);
    const notificationsList = document.getElementById("notificationsList");

    checkboxInput.disabled = true; // Disable the checkbox to prevent multiple clicks

    // Send AJAX request to mark the notification as acknowledged
    fetch("/tutor/notification/acknowledge?id=" + notificationId, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        }
    })
        .then(response => {
            // Check if the response was successful (status 200)
            if (response.ok) {
                return response.text(); // Parse the response as text
            } else if (response.status === 404) {
                // If the response was 404 (Not Found), handle that case
                throw new Error("Notification not found");
            } else {
                // If any other error occurs (e.g., 500, 400), throw an error
                throw new Error("Something went wrong with the request");
            }
        })
        .then(message => {
            notificationElement.classList.add("opacity-0", "transition-opacity", "duration-300"); // Fade out the notification
            // Wait for the fade then remove the element
            setTimeout(() => {
                notificationsList.removeChild(notificationElement);
                // If the list is empty, show a message
                if (notificationsList.children.length === 0) {
                    const noNotificationsMessage = document.createElement("li");
                    noNotificationsMessage.classList.add("py-4", "px-4", "flex", "items-start", "justify-center");
                    noNotificationsMessage.innerHTML = "<p class='text-sm text-gray-500 text-center'>No notifications.</p>";
                    notificationsList.appendChild(noNotificationsMessage);
                }
            }, 300)
        })
        .catch(error => {
            console.error("Error:", error); // Log the error
            checkboxInput.disabled = false; // Re-enable the checkbox
            checkboxInput.checked = false; // Uncheck the checkbox
        });
}


/**
 * Initialise the Google Map, adds markers for each placement address, and sets up an info window for each marker.
 * Called by the Google Maps API when it is loaded.
 */
function initGoogle() {
    // Map initialisation
    map = new google.maps.Map(document.getElementById("map"), {
        center: {lat: 52.6219263, lng: -1.1271866},
        zoom: 10,
    });

    // Iterate through JSON, extracting the placeId and adding a marker to the map, and info window with company name, address, placement id, and student name
    const placementAddresses = JSON.parse(document.getElementById("map").getAttribute("data-placement-addresses"));
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
                                <a href="/tutor/placement/view?id=${placementId}"><p class="text-sm text-teal-600 hover:underline">Placement ID: #${placementId}</p></a>
                                <p class="text-sm text-gray-600">Student: ${studentName}</p>
                            </div>
                        `);
                    infoWindow.open(marker.getMap(), marker);
                });
            }
        });
    }
}


/**
 * When the page loads, adds an event listener to each notification checkbox
 */
document.addEventListener("DOMContentLoaded", () => {
    // Get all the checkboxes for notifications
    const notificationCheckboxes = document.querySelectorAll("#notificationsList input[type='checkbox']");
    // Add an event listener for each checkbox
    for (const checkboxInput of notificationCheckboxes) {
        checkboxInput.addEventListener("change", () => acknowledgeNotification(checkboxInput));
    }
});