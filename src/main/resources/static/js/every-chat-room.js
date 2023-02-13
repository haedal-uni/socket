let connectingElement = $(".connecting");
let username = localStorage.getItem('wschat.sender');
let messageForm = document.querySelector('#messageForm');
let messageInput = document.querySelector('#message');
let roomId = null;
let sessionId = null;
let joinInterval = null;
let response;
let colors = ['#2196F3', '#32c787', '#00BCD4', '#ff5652', '#ffc107', '#ff85af', '#FF9800', '#39bbb0'];

$(document).ready(function () {
	start();
});

function start() {
	$.ajax({
		type: "GET", url: `/join`, contentType: 'application/json', async:true, processData: false,
		beforeSend: function() {
			connectingElement.text("다른 user가 접속할 때 까지 대기중입니다.")
			joinInterval = setInterval(function() {
				connectingElement.text("현재 접속을 기다리고 있는 중입니다.")
			}, 2000);
		},
		success: function(chatMessage) {
			response = JSON.stringify(chatMessage)
			clearInterval(joinInterval);
			if (!response){
				return;
			}
			let message = JSON.parse(response);
			if (message.type === 'SUCCESS') {
				connectingElement.text("모두 접속하여 채팅방이 open 되었습니다.");
				sessionId = message.sessionId;
				roomId = message.roomId;
				connect(true)
			} else if (message.type === 'TIMEOUT') {
				connectingElement.text("시간 초과 다시 시도해주세요");
			}
		}, error: function(jqxhr) {
			console.log("http staus " + JSON.stringify(jqxhr))
			clearInterval(joinInterval);
			connectingElement.text("다시 시도해주세요")
		}, complete: function() {
			clearInterval(joinInterval);
		},
	})
}
function connect(event) {
	if (username) {
		let socket = new SockJS('/ws/chat');
		stompClient = Stomp.over(socket);
		stompClient.connect({roomId : roomId}, onConnected, onError);
		// handleWebSocketConnectListener 에서 header에 roomId값 보내기
	}
}

function onConnected() {
	setTimeout(function(){
		connectingElement.text("");
	}, 1900)

	setTimeout( function() {
		stompClient.subscribe('/every-chat/' + roomId, onMessageReceived);
		//(Object) subscribe(destination, callback, headers = {})

		stompClient.send("/app/every-chat/addUser", {}, JSON.stringify({roomId: roomId, sender: username, type: 'JOIN'}))
		//(void) send(destination, headers = {}, body = '')
		$("#message").removeAttr("disabled");
	}, 2100)


}

function onError(error) {
	connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
	connectingElement.style.color = 'red';
}

// 메세지 보내기
function sendMessage(event) {
	let messageContent = messageInput.value.trim();
	if (messageContent && stompClient) {
		let chatMessage = {
			roomId: roomId, sender: username, message: messageInput.value, type: 'TALK'
		};
		stompClient.send("/app/every-chat/message/"+ roomId, {}, JSON.stringify(chatMessage));
		messageInput.value = '';
	}
	event.preventDefault(); // 계속 바뀌는 것을 방지함
}

function onMessageReceived(payload) { // 메세지 받기
	let message = JSON.parse(payload.body);
	let messageElement = document.createElement('li');
	if (message.type === 'JOIN') {
		messageElement.classList.add('event-message');
		message.message = message.sender + ' 님이 입장하셨습니다. ';
	} else if (message.type === 'LEAVE' && message.sender != "admin") {
		messageElement.classList.add('event-message');
		message.message = message.sender + ' 님이 나가셨습니다.';
	} else if (message.type === 'TALK' && message.message != null) {
		messageElement.classList.add('chat-message'); // 채팅 메세지
		let avatarElement = document.createElement('i');
		let avatarText = document.createTextNode(message.sender[0]);
		avatarElement.appendChild(avatarText);
		avatarElement.style['background-color'] = getAvatarColor(message.sender);
		messageElement.appendChild(avatarElement);
		let usernameElement = document.createElement('span');
		let usernameText = document.createTextNode(message.sender);
		usernameElement.appendChild(usernameText);
		messageElement.appendChild(usernameElement);
	}
	let textElement = document.createElement('p');
	if (message.message != null) {
		let messageText = document.createTextNode(message.message);
		textElement.appendChild(messageText);
		messageElement.appendChild(textElement);
		messageArea.appendChild(messageElement);
		messageArea.scrollTop = messageArea.scrollHeight;
	}
}

function getAvatarColor(messageSender) {
	let hash = 0;
	for (let i = 0; i < messageSender.length; i++) {
		hash = 31 * hash + messageSender.charCodeAt(i);
	}
	let index = Math.abs(hash % colors.length);
	return colors[index];
}

messageForm.addEventListener('submit', sendMessage, true)