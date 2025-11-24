/**
 * @file placements.js
 * @description JavaScript for the placements page.
 */

/**
 * Initialise the charts on the placements page.
 */
function initCharts() {
    var placementsByProgramChartData = JSON.parse(document.getElementById("placementsByProgramDiv").getAttribute("data-placements-by-program-chart-data"));
    var remoteVsOnsiteChartData = JSON.parse(document.getElementById("removeVsOnsideDiv").getAttribute("data-remove-vs-onsite-chart-data"));
    var travelArrangementsChartData = JSON.parse(document.getElementById("travelArrangementsDiv").getAttribute("data-travel-arrangements-chart-data"));
    var residentialArrangementsChartData = JSON.parse(document.getElementById("residentialArrangementsDiv").getAttribute("data-residential-arrangements-chart-data"));

    // Set the chart colours
    placementsByProgramChartData.datasets[0].backgroundColor = generateChartColours(placementsByProgramChartData.labels.length);
    remoteVsOnsiteChartData.datasets[0].backgroundColor = generateChartColours(remoteVsOnsiteChartData.labels.length);
    travelArrangementsChartData.datasets[0].backgroundColor = generateChartColours(travelArrangementsChartData.labels.length);
    residentialArrangementsChartData.datasets[0].backgroundColor = generateChartColours(residentialArrangementsChartData.labels.length);

    // Initialise the charts - if there is no data to display, show a message
    if (placementsByProgramChartData.datasets[0].data.length !== 0) {
        document.getElementById("placementByProgramNoData").classList.add("hidden");
        document.getElementById("placementsByProgramDiv").classList.remove("hidden");
        const placementsByProgramChart = new Chart("placementsByProgramChart", {
            type: "pie",
            data: placementsByProgramChartData,
            options: {
                maintainAspectRatio: false,
                responsive: true,
            }
        });
    }

    if (remoteVsOnsiteChartData.datasets[0].data.length !== 0) {
        document.getElementById("remoteVsOnsiteNoData").classList.add("hidden");
        document.getElementById("removeVsOnsideDiv").classList.remove("hidden");
        const remoteVsOnsiteChart = new Chart("remoteVsOnsiteChart", {
            type: "doughnut",
            data: remoteVsOnsiteChartData,
            options: {
                maintainAspectRatio: false,
                responsive: true,
            }
        });
    }

    if (travelArrangementsChartData.datasets[0].data.length !== 0) {
        document.getElementById("travelArrangementsNoData").classList.add("hidden");
        document.getElementById("travelArrangementsDiv").classList.remove("hidden");
        const travelArrangementsChart = new Chart("travelArrangementsChart", {
            type: "bar",
            data: travelArrangementsChartData,
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

    if (travelArrangementsChartData.datasets[0].data.length !== 0) {
        document.getElementById("residentialArrangementsNoData").classList.add("hidden");
        document.getElementById("residentialArrangementsDiv").classList.remove("hidden");
        const residentialArrangementsChart = new Chart("residentialArrangementsChart", {
            type: "bar",
            data: residentialArrangementsChartData,
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
}

/**
 * When the document has loaded, initialise the charts on the page
 */
document.addEventListener("DOMContentLoaded", function () {
    initCharts();
});