let messageArea = document.querySelector('.body');
let nickname = localStorage.getItem('wschat.sender');
let roomName = localStorage.getItem('wschat.roomName');
let chatArea = document.querySelector('.chat');
let messageInput = document.querySelector('#message-input');
let roomId = localStorage.getItem('wschat.roomId');
let stompClient = null;
let token = localStorage.getItem('token');

messageInput.addEventListener("keyup", function (event) {
	if (event.keyCode === 13) {
		event.preventDefault();
		document.getElementById("sendButton").click();
	}
});

function openChat() {
	if (nickname == null || nickname == undefined){
		findToken()
		setInterval(nickname, 1000)
	}
	document.getElementById('container').classList.add('open');
	$.ajax({
		type: "POST", url: `/room`, data: nickname, contentType: false, processData: false, success: function(response) {
			localStorage.setItem('wschat.roomName', nickname);
			localStorage.setItem('wschat.roomId', response["roomId"]);
			if (!document.getElementById("needChat")) {
				let temp = `
		<div id="needChat" class="conversation" onclick="joinChat()">
        <div class="top">
			<span class="badge">0</span>
			<span class="title">리스트에서 제목</span>
			<span class="time">18:10</span>
		</div>
		<div class="bottom">
			<span class="user">마지막에 작성한 user</span>
			<span class="message">마지막 메세지</span>
		</div>
	</div>
			`
				$(".conversations").append(temp);

				if(!document.getElementById("randomChat")){
					let temp_html = `	
				<div id ="randomChat" class="conversation" onclick="randomChat()">
					<div class="top">
					<span></span>
					<span class="title">랜덤 채팅방 참여하기</span>
					<span class="time">15/05/2019</span>
					</div>
					<div class="bottom">
					<span class="user">adme</span>
					<span class="message">랜덤으로 2명 채팅이 가능합니다.</span>
					</div>
				</div>`
					$(".conversations").append(temp_html);

				}
			}
		}
	});
}

function randomChat(){
}
function onMessageReceived(payload) { // 메세지 받기
	let message;
	try {
		message = JSON.parse(payload.body);
	} catch (SyntaxError) {
		message = payload;
	}
	let divName;
	if (message.sender == nickname) {
		divName = "user right"
	} else {
		divName = "user left"
	}
	let messageElement = document.createElement('div');
	if (message.type === 'JOIN') {
		if (message.sender != "admin") {
			message.message = message.sender + ' 님 안녕하세요';
			seperator(message.message);
		}
	} else if (message.type === 'LEAVE' && message.sender != "admin") {
		message.message = message.sender + ' 님이 나가셨습니다.';
		seperator(message.message);
	} else if (message.type === 'DELETE') {
		message.message = roomName + ' 님 채팅이 종료되어 ' + '현재 시간 [ ' + ms + ' ]  ' + ' 으로 부터 5분 뒤에 삭제될 예정입니다.';
		seperator(message.message);
	} else if (message.type === 'TALK' && message.message != null) {
		let temp = `
    <div class="${divName}">
    <i class = "avatar">${message.sender[0]}</i>
        <div class="messages">
            <div class="message">
                <div class="message-container">
                    <div class="message-sender">${message.sender}</div>
                    <p class="text">${message.message}</p>
                </div>
            <div class="message-time">12:08</div>
        </div>
    </div>
</div>
		`
		$(".body").append(temp)
		chatArea.scrollTop = chatArea.scrollHeight;
	}
}

function seperator(message){
	let temp = `
<div class="seperator">
	<div class="line"></div>
    <div class="moment">${message}</div>
    <div class="line"></div>
</div>`
	$(".body").append(temp)
}
function closeChat() {
	document.getElementById('container').classList.remove('open');
	document.querySelector('.list').classList.remove('close');
	document.querySelector('.chat').classList.add('close');
	document.getElementById('back').classList.add('hidden');
	closeDrawer();
}

function backChat() {
	document.querySelector('.list').classList.remove('close');
	document.querySelector('.chat').classList.add('close');
	document.getElementById('back').classList.add('hidden');
	document.querySelector('.header .title').textContent = 'Chat';
	closeDrawer();
}

function openDrawer() {
	document.querySelector('.drawer').classList.remove('close');
}

function closeDrawer() {
	document.querySelector('.drawer').classList.add('close');
}

function joinChat() {
	document.querySelector('.chat').classList.remove('close');
	let roomId = localStorage.getItem('wschat.roomId')
	connect()
	getFile()
}

function connect() {
	if (nickname) {
		let socket = new SockJS('/ws');
		stompClient = Stomp.over(socket);
		stompClient.connect({Authorization: token}, onConnected, onError);
	}
}

function onConnected() {
	let roomId = localStorage.getItem('wschat.roomId')
	stompClient.subscribe('/topic/public/' + roomId, onMessageReceived);
	//(Object) subscribe(destination, callback, headers = {})
	//stompClient.send("/app/chat/addUser", {Authorization:token}, JSON.stringify({roomId: roomId, sender: nickname, type: 'JOIN'}))
	stompClient.send("/app/chat/addUser", {Authorization: token}, JSON.stringify({roomId: roomId, type: 'JOIN'}))
	//(void) send(destination, headers = {}, body = '')
}

function onError(error) {
	connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
	connectingElement.style.color = 'red';
}

// 메세지 보내기
function sendMessage() {
	let messageContent = messageInput.value.trim();
	if (messageContent && stompClient) {
		let chatMessage = {
			roomId: roomId, sender: nickname, message: messageContent, type: 'TALK'
		};
		saveFile(chatMessage)
		stompClient.send("/app/chat/sendMessage", {}, JSON.stringify(chatMessage));
		messageInput.value = '';
	}
}

function saveFile(chatMessage) {
	$.ajax({
		type: "POST",
		url: `/room/enter/` + roomId + '/' + roomName,
		data: JSON.stringify(chatMessage),
		contentType: 'application/json',
		processData: false,
		success: function(response) {
		}
	});
}

let isRun = false;

function getFile() {
	if (isRun == true) {
		return;
	}
	isRun = true;
	$.ajax({
		type: "GET", url: `/room/enter/` + roomId + '/' + roomName, contentType: false, processData: false, success: function(response) {
			for (let i = 0; i < response.length; i++) {
				onMessageReceived(response[i])
			}
		}
	})
}

function alarmMessage() {
	if ($("#sendButton").click) {
		fetch(`/room/publish?sender=${nickname}&roomId=${roomId}`);
	}
}
alarmMessage()

document.addEventListener('DOMContentLoaded', function() {
	document.querySelectorAll('.conversation').forEach(function(conversation) {
		conversation.addEventListener('click', function() {
			document.querySelector('.list').classList.add('close');
			document.querySelector('.chat').classList.remove('close');
			document.getElementById('back').classList.remove('hidden');
			document.querySelector('.header .title').textContent = 'Title';
		});
	});
});

// Fire once
document.querySelectorAll('focus.auto-expand, textarea.auto-expand').forEach(item => {
	item.addEventListener('click', function(e) {
		if (e.currentTarget.dataset.triggered) return;
		e.currentTarget.dataset.triggered = true;
		var savedValue = this.value;
		this.value = '';
		this.baseScrollHeight = this.scrollHeight;
		this.value = savedValue;
	});
});
// Fire anytime
document.querySelectorAll('input.auto-expand, textarea.auto-expand').forEach(item => {
	item.addEventListener('input', function(e) {
		var minRows = this.getAttribute('data-min-rows') | 0;
		this.rows = minRows;
		rows = Math.ceil((this.scrollHeight - this.baseScrollHeight) / 17);
		this.rows = minRows + rows;
		this.scrollTop = this.scrollHeight;
	});
});