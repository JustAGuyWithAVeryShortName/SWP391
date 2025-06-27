let stompClient = null;

window.addEventListener('DOMContentLoaded', () => {
    // 1. Tải lịch sử chat
    fetch('/api/chat/history')
        .then(response => response.json())
        .then(messages => {
            messages.forEach(message => {
                appendMessage(message);
            });
        });

    // ✅ 2. Delay 200ms rồi kết nối WebSocket (chờ cookie sau login)
    setTimeout(() => {
        connectSocket();
    }, 500);

    // 3. Gửi tin nhắn khi submit
    const chatForm = document.getElementById('chat-form');
    if (chatForm) {
        chatForm.addEventListener('submit', function (event) {
            event.preventDefault();
            const content = document.getElementById('message-content').value;
            if (content.trim() !== "") {
                stompClient.send("/app/chat.send", {}, JSON.stringify({ content }));
                document.getElementById('message-content').value = '';
            }
        });
    }
});


function connectSocket() {
    if (stompClient && stompClient.connected) {
        console.log("Already connected, skipping reconnect.");
        return;
    }

    const socket = new SockJS("http://localhost:8080/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/public', function (messageOutput) {
            const message = JSON.parse(messageOutput.body);
            console.log("Debug message:", message);
            appendMessage(message);
        });
    }, function (error) {
        console.error('STOMP error:', error);
    });
}

function appendMessage(message) {
    const table = document.getElementById('chat-table');
    if (!table) return;

    const row = table.insertRow(-1);
    const cell = row.insertCell(0);

    const lastName = message.lastName || "";
    const firstName = message.firstName || "";
    const fullName = `${lastName} ${firstName}`.trim();

    const nameSpan = document.createElement('span');
    nameSpan.textContent = fullName;

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
    if (wrapper) {
        wrapper.scrollTop = wrapper.scrollHeight;
    }
}
