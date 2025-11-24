/**
 * @file dataTableSetup.js
 * @description Initialises the DataTables library for the page, setting up the table with the required options.
 */


/**
 * Initialises the DataTable library for the table on the page, adding search, pagination, and ordering functionality.
 *
 * @param {HTMLTableElement} table The table element to initialise DataTable on
 * @param {HTMLInputElement} searchInput The input element for search functionality
 */
    function initTable(table, searchInput) {

        // Build the column definitions for the table, setting the custom type for id, date, and view columns
        const columnDefs = [];
        let idColumnIndex = -1; // Store the id column index to sort by ID descending if it exists
        table.querySelectorAll("thead th").forEach((th, index) => {
            let columnName = th.textContent.trim().toLowerCase();
            if (columnName === "id") { // If the column name is "ID", set the type to "id-num" custom sorting
                columnDefs.push({ targets: index, type: "id-num" });
                idColumnIndex = index;
            }
            if (columnName.includes("date")) { // If the column name includes "Date", set the type to "date-dd-mm-yy" custom sorting
                columnDefs.push({ targets: index, type: "date-dd-mm-yy" });
            }
            if (columnName.includes("view")) { // If the column name includes "View", set the column to not be orderable or searchable, as it will be a button
                columnDefs.push({ targets: index, orderable:false, searchable:false });
            }
        });

    const dataTable = new DataTable(table, {
        responsive: false,
        paging: true,
        searching: true,
        ordering: true,
        info: true,
        dom: "trip", // Customise the table layout. l - length, f - filter (search), t - table, r - processing, i - info, p - pagination
        pageLength: 10,
        language: {
            emptyTable: "<span class='text-center text-xs text-gray-500 py-2'>No records found</span>",
            info: "<span class='text-center text-xs text-gray-500 py-2'>Showing _START_ to _END_ of _TOTAL_ entries</span>",
            infoEmpty: "<span class='text-center text-xs text-gray-500 py-2'>Showing 0 to 0 of 0 entries</span>",
            infoFiltered: "<span class='text-center text-xs text-gray-500 py-2'>(filtered from _MAX_ total entries)</span>",
            zeroRecords: "<span class='text-center text-xs text-gray-500 py-    2'>No matching records found</span>",
        },
        columnDefs: columnDefs,
        order: idColumnIndex !== -1 ? [[idColumnIndex, "desc"]] : [[0, "asc"]], // Sort by ID descending if exists, otherwise first column ascending
    });

    // If the table has a custom search input, add an event listener to it and update the table search on keyup
    if (searchInput) {
        searchInput.addEventListener("keyup", function () {
            dataTable.search(this.value).draw();
        });
    }
}


/**
 * Custom ordering function for the DataTable library to order by ID numbers
 * @param id The id to order
 * @returns {number} The id number
 */
$.fn.dataTable.ext.type.order['id-num-pre'] = function (id) {
    return parseInt(id.replace('#', ''), 10) || 0;
};


/**
 * Custom ordering function for the DataTable library to order by dates in the format dd/mm/yy
 * @param date The date to order
 * @returns {number} The timestamp of the date
 */
$.fn.dataTable.ext.type.order["date-dd-mm-yy-pre"] = function(date) {
    if (!date) return 0;
    let [day, month, year] = date.split("/");
    if (!day || !month || !year) return 0;
    year = "20" + year;
    return new Date(year, month - 1, day).getTime();
};


/**
 * Initialises all tables/search bars when the DOM content is loaded
 */
document.addEventListener("DOMContentLoaded", function () {
    const tableDivs = document.querySelectorAll(".tableDiv");
    for (const tableDiv of tableDivs) {
        const table = tableDiv.querySelector("table");
        const searchInput = tableDiv.querySelector(".tableSearch");
        if (table) {
            initTable(table, searchInput);
        }
    }
});