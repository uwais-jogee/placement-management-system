/**
 * @file dashboard.js
 * @description JavaScript for the admin dashboard page.
 */

/**
 * Initialises the Google Maps API and adds markers to the map for each placement address
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
                                <a href="/admin/placement/view?id=${placementId}"><p class="text-sm text-teal-600 hover:underline">Placement ID: #${placementId}</p></a>
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
 * Initialises the chart.js chart for the placements over time chart
 */
function initChart() {
    var chartData = JSON.parse(document.getElementById("placementsOverTimeDiv").getAttribute("data-chart-data"));

    // Check if the chartData is empty
    if (Object.keys(chartData).length === 0) {
        // If there is no data, display the no data message and hide the chart
        document.getElementById("placementsOverTimeNoData").classList.remove("hidden");
        document.getElementById("placementsOverTimeDiv").classList.add("hidden");
        return;
    }

    chartData.datasets[0].pointRadius = 2; // Decrease the size of the points from 3 to 2
    chartData.datasets[0].tension = 0.4; // Make the line curved
    const placementsOverTimeChart = new Chart("placementsOverTimeChart", {
        type: "line",
        data: chartData,
        options: {
            maintainAspectRatio: false,
            responsive: true,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    ticks: {
                        callback: function(value) {
                            return Number.isInteger(value) ? value : ''; // Show only whole numbers
                        }
                    }
                }
            }
        }
    });
}

/**
 * Event listener for the DOMContentLoaded event - initialises the chart.js chart
 */
document.addEventListener("DOMContentLoaded", () => {
    initChart();
});