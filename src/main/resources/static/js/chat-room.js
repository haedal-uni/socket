var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var endChatRoom = document.querySelector('#endChat');
var stompClient = null;
var username = localStorage.getItem('wschat.sender');
let roomName = localStorage.getItem('wschat.roomName');
var colors = [
	'#2196F3', '#32c787', '#00BCD4', '#ff5652',
	'#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];
$(".title").text(roomName + "님 ")
$("#h2-chatRoomName").text(roomName + "님 고객센터 채팅방입니다.")
function connect(event) {
	if(username) {
		usernamePage.classList.add('hidden');
		chatPage.classList.remove('hidden');

		var socket = new SockJS('/ws');
		stompClient = Stomp.over(socket);

		stompClient.connect({}, onConnected, onError);
	}
	event.preventDefault();
}

function onConnected() {
	stompClient.subscribe('/topic/public', onMessageReceived);

	// send(path, header, message)로 메세지를 보낼 수 있음
	stompClient.send("/app/chat/addUser",
		{},
		JSON.stringify({sender: username, type: 'JOIN'})
	)
	//(void) send(destination, headers = {}, body = '')
	//명명된 목적지 "/app/chat.adduser"로 메세지를 보냄

	connectingElement.classList.add('hidden');
}

function onError(error) {
	connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
	connectingElement.style.color = 'red';
}

// 메세지 보내기
function sendMessage(event) {
	var messageContent = messageInput.value.trim();
	if(messageContent && stompClient) {
		var chatMessage = {
			sender: username,
			message: messageInput.value,
			type: 'TALK'
		};
		stompClient.send("/app/chat/sendMessage", {}, JSON.stringify(chatMessage));
		messageInput.value = '';
	}
	event.preventDefault(); // 계속 바뀌는 것을 방지함
}

function onMessageReceived(payload) { // 메세지 받기
	var message = JSON.parse(payload.body);
	var messageElement = document.createElement('li');
	if(message.type === 'JOIN' && message.sender != "admin") {
		messageElement.classList.add('event-message');
		message.message = message.sender + ' 님 안녕하세요';
	} else if (message.type === 'LEAVE'&& message.sender != "admin" ) {
		messageElement.classList.add('event-message');
		message.message = message.sender + ' 님이 나가셨습니다.';
	} else if (message.type === 'TALK' && message.message != null) {
		messageElement.classList.add('chat-message'); // 채팅 메세지

		//아래는 아바타 html + css 설정인듯
		var avatarElement = document.createElement('i');
		var avatarText = document.createTextNode(message.sender[0]);
		avatarElement.appendChild(avatarText);
		avatarElement.style['background-color'] = getAvatarColor(message.sender);
		messageElement.appendChild(avatarElement);
		var usernameElement = document.createElement('span');
		var usernameText = document.createTextNode(message.sender);
		usernameElement.appendChild(usernameText);
		messageElement.appendChild(usernameElement);
	}

	var textElement = document.createElement('p');
	if (message.message != null){
		var messageText = document.createTextNode(message.message);
		textElement.appendChild(messageText);
		messageElement.appendChild(textElement);
		messageArea.appendChild(messageElement);
		messageArea.scrollTop = messageArea.scrollHeight;
	}
}

function getAvatarColor(messageSender) {
	var hash = 0;
	for (var i = 0; i < messageSender.length; i++) {
		hash = 31 * hash + messageSender.charCodeAt(i);
	}
	var index = Math.abs(hash % colors.length);
	return colors[index];
}

function deleteRoom() {
	let url = new URL(window.location.href).pathname;
	let roomId = url.split("enter/")[1]
	let delConfrim = confirm("채팅방을 삭제하실 건가요?")
	if(delConfrim){
		$.ajax({
			type: "DELETE", url: `/room/one/` + roomId, contentType: false, processData: false, success: function(response) {
				alert(username + "님 채팅방이 삭제되었습니다.");
				localStorage.removeItem('wschat.roomId')
				localStorage.removeItem('wschat.roomName')
				location.href = "/room/"
			}
		})
	}
}

if (username == "admin") {
	endChatRoom.classList.remove('hidden');
}
function isRoom(roomId){
	$.ajax({
		type: "GET", url: `/room/` + roomId, contentType: false, processData: false, success: function(response) {
			if(!response){
				location.href = "/room/"
			}
		}
	})
}

let url = new URL(window.location.href).pathname;
let roomId = url.split("enter/")[1]
isRoom(roomId)
usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)