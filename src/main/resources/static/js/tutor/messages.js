/**
 * @file messages.js
 * @description JavaScript for the tutor messages page
 */


// Global variables
let stompClient;
const hiddenData = document.getElementById("hiddenData");
let studentUsername
let studentFirstName
let studentLastName
let placementId


/**
 * Fetches messages for the selected student using AJAX and calls a function to update the chat UI
 * @param studentDiv {HTMLElement} The student div that was clicked to display messages for
 */
function fetchMessages(studentDiv) {
    // Build the URL with the placement ID as a query parameter
    const id = studentDiv.getAttribute("data-placement-id");

    // Visually select the student that has been clicked
    // Remove the selected class from all students
    const students = document.querySelectorAll("[data-placement-id]");
    students.forEach((student) => {
        student.classList.remove("bg-gray-100");
    });
    // Add the selected class to the clicked student
    studentDiv.classList.add("bg-gray-100");

    // Use the Fetch API to request messages
    fetch("/tutor/messages/get?id=" + id, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error(`Failed to fetch messages: ${response.statusText}`);
            }
            return response.json();
        })
        .then((messages) => {
            // Set the global variables for the selected student
            studentUsername = studentDiv.getAttribute("data-student-username");
            studentFirstName = studentDiv.getAttribute("data-student-first-name");
            studentLastName = studentDiv.getAttribute("data-student-last-name");
            placementId = id;
            // Call a function to update the chat UI with the fetched messages
            updateChatUI(messages);
            scrollBottomMessages()
            // Enable the message input field and submit button
            document.getElementById("messageInput").disabled = false;
            document.getElementById("messageSubmitButton").disabled = false;
        })
        .catch((error) => {
            console.error("Error fetching messages:", error);
        });
}


/**
 * Updates the message chat with the given list of messages that has been fetched
 * @param messages {Array} The list of messages to display
 */
function updateChatUI(messages) {
    const messagesArea = document.getElementById("messagesArea");
    messagesArea.innerHTML = ""; // Clear existing messages

    messages.forEach((message) => {
        // Determine if the sender is the current user
        const isCurrentUser = message.senderUsername === hiddenData.getAttribute("data-tutor-username");

        // Create a div for the message
        const messageDiv = document.createElement("div");
        if (isCurrentUser) {
            messageDiv.className = "ml-auto bg-teal-100 p-2 rounded-lg shadow-sm max-w-xs break-words";
        } else {
            messageDiv.className = "mr-auto bg-gray-100 p-2 rounded-lg shadow-sm max-w-xs break-words";

            // Notify the server that the message has been read
            stompClient.send('/app/markAsRead', {}, JSON.stringify({messageId: message.id}));
        }

        // Add sender's name
        const senderName = document.createElement("p");
        senderName.className = "text-sm font-medium text-gray-700";
        senderName.textContent = message.senderFirstName + " " + message.senderLastName;
        messageDiv.appendChild(senderName);

        // Add message content
        const messageContent = document.createElement("p");
        messageContent.className = "text-sm text-gray-800";
        messageContent.textContent = message.content;
        messageDiv.appendChild(messageContent);

        // Add timestamp
        const timestamp = document.createElement("p");
        timestamp.className = "text-xs text-gray-500";
        timestamp.textContent = formatDate(message.dateTime);
        messageDiv.appendChild(timestamp);

        // Append the message div to the messages area
        messagesArea.appendChild(messageDiv);
    });
}


/**
 * Scrolls the messages area to the bottom
 */
function scrollBottomMessages() {
    const messagesArea = document.getElementById("messagesArea");
    messagesArea.scrollTop = messagesArea.scrollHeight;
}


/**
 * Formats the given date to a string in the format "dd/MM/yy, HH:mm"
 * @param date {Date} The date to format
 * @returns {string} The formatted date string
 */
function formatDate(date) {
    const d = new Date(date); // Create Date object
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = String(d.getFullYear()).slice(-2);
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year}, ${hours}:${minutes}`;
}


/**
 * Connects to the WebSocket endpoint using SockJS and STOMP. Subscribes to the private message queue for this user.
 */
function connect() {
    const socket = new SockJS(window.location.origin + "/ws"); // Backend WebSocket endpoint
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        // Subscribe to the private message queue for this user
        stompClient.subscribe('/user/queue/messages', function (message) {
            const messageData = JSON.parse(message.body);
            showMessage(messageData);
        });
    });
}


/**
 * Sends a message to the student via the WebSocket connection
 */
function sendMessage() {
    const messageInput = document.getElementById("messageInput");
    const messageContent = messageInput.value;

    // Check the message is not empty, and if the student details are set
    if (!messageContent.trim() || studentUsername == null || studentFirstName == null || studentLastName == null) {
        return;
    }

    const messageDTO = {
        id: null, // Will be generated by the server
        content: messageContent,
        senderUsername: hiddenData.getAttribute("data-tutor-username"),
        senderFirstName: hiddenData.getAttribute("data-tutor-first-name"),
        senderLastName: hiddenData.getAttribute("data-tutor-last-name"),
        receiverUsername: studentUsername,
        receiverFirstName: studentFirstName,
        receiverLastName: studentLastName,
        placementId: placementId,
        dateTime: new Date().toISOString()
    };

    stompClient.send('/app/sendMessage', {}, JSON.stringify(messageDTO));

    // Clear the input field after sending
    messageInput.value = "";
}


/**
 * Displays a newly arrived message in the messages area. Called when a new message arrives via WebSocket.
 * @param message {object} The message object to be displayed
 */
function showMessage(message) {
    // Check if the message that has arrived is for the currently selected student
    if (message.receiverUsername !== studentUsername && message.senderUsername !== studentUsername) {
        return;
    }

    const messagesArea = document.getElementById("messagesArea");
    const messageElement = document.createElement("div");
    messageElement.classList.add('p-2', 'rounded-lg', 'shadow-sm', 'max-w-xs', 'break-words');

    if (message.senderUsername === hiddenData.getAttribute("data-tutor-username")) {
        messageElement.classList.add('ml-auto', 'bg-teal-100');
    } else {
        messageElement.classList.add('mr-auto', 'bg-gray-100');
        // Notify the server that the message has been read
        stompClient.send('/app/markAsRead', {}, JSON.stringify({messageId: message.id}));
    }

    messageElement.innerHTML = `
            <p class="text-sm font-medium text-gray-700">${message.senderFirstName} ${message.senderLastName}</p>
            <p class="text-sm text-gray-800">${message.content}</p>
            <p class="text-xs text-gray-500">${formatDate(message.dateTime)}</p>
        `;
    messagesArea.appendChild(messageElement);
    scrollBottomMessages();
}


/**
 * Validates the form and sends the message if valid
 */
function onMessageFormSubmit() {
    const form = document.getElementById("messageForm");

    if (form.checkValidity()) {
        sendMessage();
    } else {
        form.reportValidity();
    }
}


/**
 * When the page loads, initialises the event listeners and WebSocket connection
 */
document.addEventListener("DOMContentLoaded", function () {
    // Add onclick event listeners to each student div
    const placementDivs = document.querySelectorAll(".placementMessageChats");
    for (const placementDiv of placementDivs) {
        placementDiv.addEventListener("click", () => fetchMessages(placementDiv));
    }

    // Event listener for sending the message form
    const messageSubmitButton = document.getElementById("messageSubmitButton");
    messageSubmitButton.addEventListener("click", onMessageFormSubmit);

    // Initialise WebSocket connection
    connect();
    scrollBottomMessages()
});