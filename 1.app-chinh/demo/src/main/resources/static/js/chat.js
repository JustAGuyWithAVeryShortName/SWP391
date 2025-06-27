const socket = new SockJS('/ws'); // endpoint WebSocket backend đã cấu hình
const stompClient = Stomp.over(socket);


window.addEventListener('DOMContentLoaded', () => {
    fetch('/api/chat/history')
        .then(response => response.json())
        .then(messages => {
            messages.forEach(message => {
                appendMessage(message);
            });
        });
});


stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/public', function (messageOutput) {
        const message = JSON.parse(messageOutput.body);
        console.log("Debug message:", message);
        appendMessage(message); // Gọi hàm hiển thị
    });
});

document.getElementById('chat-form').addEventListener('submit', function (event) {
    event.preventDefault();
    const content = document.getElementById('message-content').value;
    if (content.trim() !== "") {
        stompClient.send("/app/chat.send", {}, JSON.stringify({ content: content }));
        document.getElementById('message-content').value = '';
    }
});

function appendMessage(message) {
    const table = document.getElementById('chat-table');
    const row = table.insertRow(-1);
    const cell = row.insertCell(0);

    // ✅ Không còn message.user -> dùng trực tiếp
    const lastName = message.lastName || "";
    const firstName = message.firstName || "";
    const fullName = `${lastName} ${firstName}`.trim();

    const nameSpan = document.createElement('span');
    nameSpan.textContent = fullName;

    // ✅ So sánh username trực tiếp
    if (
        message.username === currentUsername ||
        message.email === currentUsername
    ) {
        nameSpan.style.color = "blue";
        nameSpan.style.fontWeight = "bold";
    }

    const messageSpan = document.createElement('span');
    messageSpan.textContent = `: ${message.content}`;

    cell.appendChild(nameSpan);
    cell.appendChild(messageSpan);

    const wrapper = document.querySelector('.chat-table-wrapper');
    wrapper.scrollTop = wrapper.scrollHeight;
}


