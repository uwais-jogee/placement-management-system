/**
 * @file viewCompany.js
 * @description JavaScript for the view company page
 */

/**
 * Initialises the ratings chart using the JSON data from the model, set as an attribute in the ratingsChartDiv element
 */
function initChart() {
    var chartData = JSON.parse(document.getElementById("ratingsChartDiv").getAttribute("data-chart-data"));
    const ratingsChart = new Chart("ratingsChart", {
        type: "radar",
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
                r: { // Updated to "r" as this is the radial scale in radar charts
                    min: 0, // Set minimum value of the scale
                    max: 5, // Set maximum value of the scale
                    ticks: {
                        callback: function (value) {
                            return Number.isInteger(value) ? value : ''; // Show only whole numbers
                        },
                        color: "#111827", // Change the tick label color
                    },
                    pointLabels: {
                        font: {
                            size: 12 // Make the labels bigger
                        },
                        color: "#111827" // Change the label color
                    }
                }
            },
            elements: {
                line: {
                    borderWidth: 3,
                    fill: false // Make the fill transparent
                }
            }
        }
    });
}

/**
 * When the document has loaded, adds event listeners to the buttons and initialises the ratings chart
 */
document.addEventListener("DOMContentLoaded", function () {
    const placementsModal = document.getElementById("placementsModal");

    // Add event listener for each view button to populate the placements table then show the modal
    const viewButtons = document.querySelectorAll(".viewPlacementsButton");
    for (const buttonEl of viewButtons) {
        buttonEl.addEventListener("click", () => showModal(placementsModal));
    }

    // Add event listener for the modal close button
    const modalCloseButton = document.getElementById("placementsModalCloseButton");
    modalCloseButton.addEventListener("click", () => hideModal(placementsModal));

    // Initialise the ratings chart
    initChart();
});