let messageArea = document.querySelector('.body');
let nickname = localStorage.getItem('wschat.sender');
let roomName = nickname;
let chatArea = document.querySelector('.chat');
let messageInput = document.querySelector('#message-input');
let stompClient = null;
let token = localStorage.getItem('token');
let urlSearch = new URLSearchParams(location.search);
let count=0;
let connectingElement = $(".body");
let roomId = null;
function alarmCount(num){
	if (num===0){
		count=0;
	}else{
		count += num;
	}
	$(".badge").text(count);

}
function findToken() {
	let token = urlSearch.get('token')
	if (token != null && token !== localStorage.getItem('token')) {
		localStorage.setItem('token', token);
		emptyUsername(token)
	}
}

function emptyUsername(token) {
	$.ajax({
		type: "GET", url: `/find-nickname/` + token, contentType: false, processData: false, success: function(response) {
			nickname = response;
			localStorage.setItem('wschat.sender', nickname);
		}
	})

}

$(document).ready(function() {
	alarmSubscribe();
	if(token==null){
		findToken();
	}
	else if(nickname==null){
		emptyUsername(token)
	}
	alarmCount(0)
});
messageInput.addEventListener("keyup", function(event) {
	if (event.keyCode === 13) {
		event.preventDefault();
		document.getElementById("sendButton").click();
	}
});

function openChat() {
	document.getElementById('container').classList.add('open');
	let nickname = localStorage.getItem('wschat.sender');
	if (nickname!=null){
		openChatList()
	}
}

function openChatList() {
	let nickname = localStorage.getItem('wschat.sender');
	$.ajax({
		type: "POST", url: `/room`, data: nickname, contentType: false, processData: false, success: function(response) {
			localStorage.setItem('wschat.roomName', nickname);
			localStorage.setItem('wschat.roomId', response["roomId"]);
			let count = response["userChat"];
			let message = response["message"];
			if (!document.getElementById("needChat")) {
				let temp = `
		<div id="needChat" class="conversation" onclick="joinChat()">
        <div class="top">
			<span class="badge">${count}</span>
			<span class="title">리스트에서 제목</span>
			<span class="time">18:10</span>
		</div>
		<div class="bottom">
			<span class="user">${nickname}</span>
			<span class="message">${message}</span>
		</div>
	</div>
			`
				$(".conversations").append(temp);
				if (!document.getElementById("randomChat")) {
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

function onMessageReceived(payload) { // 메세지 받기
	let message;
	try {
		message = JSON.parse(payload.body);
	} catch (SyntaxError) {
		message = payload;
	}
	let divName;
	if (message.sender != "admin") {
		divName = "user right"
	} else {
		divName = "user left"
	}
	if (message.type === 'JOIN') {
		if (message.sender != "admin") {
			alarmCount(0);
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

function seperator(message) {
	let temp = `
<div class="seperator">
	<div class="line"></div>
    <div class="moment">${message}</div>
    <div class="line"></div>
</div>`
	$(".body").append(temp)
}

function closeChat() {
	$(".body").text("")
	isRun = false;
	document.getElementById('container').classList.remove('open');
	document.querySelector('.list').classList.remove('close');
	document.querySelector('.chat').classList.add('close');
	document.getElementById('back').classList.add('hidden');
	closeDrawer();
    if(stompClient==null){
		$("#randomSendButton").css("display","none")
	} else if(stompClient.connect()){
		stompClient.disconnect({});
		$("#sendButton").css("display","none")
	}
}

function backChat() {
	isRun = false;

	$(".body").text("")
	if(stompClient==null){
		$("#randomSendButton").css("display","none")
	} else if(stompClient.connect()){
		stompClient.disconnect({});
		$("#sendButton").css("display","none")
	}
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
	document.getElementById('back').classList.remove('hidden');
	connect()
	getFile()
	alarmCount(0)
	let temp = `
	<button class="btn btn-round btn-icon" id="sendButton" type="button" onclick="sendMessage()">send
<i class="fa fa-paper-plane"></i>
</button>
	`
	$("#sendButtonType").append(temp)
}

function connect() {
	let nickname = localStorage.getItem('wschat.sender');
	let token = localStorage.getItem('token');
	if (nickname) {
		let socket = new SockJS('/ws');
		stompClient = Stomp.over(socket);
		stompClient.connect({Authorization: token}, onConnected, onError);
	}
}

function onConnected() {
	let token = localStorage.getItem('token');
	let roomId = localStorage.getItem('wschat.roomId')
	stompClient.subscribe('/topic/public/' + roomId, onMessageReceived);
	//(Object) subscribe(destination, callback, headers = {})
	//stompClient.send("/app/chat/addUser", {Authorization:token}, JSON.stringify({roomId: roomId, sender: nickname, type: 'JOIN'}))
	let message = $(".message-container").last().text().trim().split("\n")[1].trim()
	stompClient.send("/app/chat/addUser", {Authorization: token}, JSON.stringify({roomId: roomId, type: 'JOIN', message: message}))
	//(void) send(destination, headers = {}, body = '')
}

function onError(error) {
	connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
	connectingElement.style.color = 'red';
}

// 메세지 보내기
function sendMessage() {
	alarmMessage()
	let nickname = localStorage.getItem('wschat.sender');
	let roomId = localStorage.getItem('wschat.roomId');
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
	let roomId = localStorage.getItem('wschat.roomId');
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
	let roomId = localStorage.getItem('wschat.roomId');
	if (isRun == true) {
		return;
	}
	//isRun = true;
	$.ajax({
		type: "GET", url: `/room/enter/` + roomId + '/' + roomName, contentType: false, processData: false, success: function(response) {
			console.log("ressss  : " + response[1])
			for (let i = 0; i < response.length; i++) {
				onMessageReceived(response[i])
			}
		}
	})
}
function timer(){
	let time = 20;
	let min = "";
	let sec = "";
	let x = setInterval(function(){
		min = parseInt(time/60);
		sec = time%60;
		connectingElement.text("  현재 1명의 접속을 기다리고 있는 중입니다.  [ " + min + " 분  " + sec + " 초 ]");
		time--;
		if (time < 0) {
			clearInterval(x);
			connectingElement.text("시간 초과 다시 시도해주세요");
		}
	}, 1000)
}
function randomChat() {
	document.querySelector('.chat').classList.remove('close');
	$.ajax({
		type: "GET", url: `/join`, contentType: 'application/json', async:true, processData: false,
		beforeSend: function() {
			connectingElement.text("다른 user가 접속할 때 까지 대기중입니다.")
			timer();
			joinInterval = setInterval(function() {
				//connectingElement.text("현재 접속을 기다리고 있는 중입니다.")

			});
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
				randomConnect(true)
			} else if (message.type === 'TIMEOUT') {
				//connectingElement.text("시간 초과 다시 시도해주세요");
				console.log("timeout!")
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
function randomConnect(event){
	let temp = `
	<button class="btn btn-round btn-icon" id="randomSendButton" type="button" onclick="randomSendMessage()">send
<i class="fa fa-paper-plane"></i>
</button>
	`
	$("#sendButtonType").append(temp)

	let nickname = localStorage.getItem('wschat.sender');
	let token = localStorage.getItem('token');
	if(nickname){
		let socket = new SockJS('/ws/chat');
		stompClient = Stomp.over(socket);
		stompClient.connect({roomId : roomId}, randomOnConnected, onError);
	}
}

function randomOnConnected(){
	setTimeout(function(){
		connectingElement.text("");
	}, 1900)

	setTimeout( function() {
		stompClient.subscribe('/every-chat/' + roomId, randomMessageReceived);
		//(Object) subscribe(destination, callback, headers = {})

		stompClient.send("/app/every-chat/addUser", {}, JSON.stringify({roomId: roomId, sender: username, type: 'JOIN'}))
		//(void) send(destination, headers = {}, body = '')
		$("#message").removeAttr("disabled");
	}, 2100)
}

function randomMessageReceived(payload){
	let message;
	try {
		message = JSON.parse(payload.body);
	} catch (SyntaxError) {
		message = payload;
	}
	let divName;
	if (message.sender != "admin") {
		divName = "user right"
	} else {
		divName = "user left"
	}
	if (message.type === 'JOIN') {
		if (message.sender != "admin") {
			message.message = message.sender + ' 님 안녕하세요';
			seperator(message.message);
		}
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
function randomSendMessage(event){
	let nickname = localStorage.getItem('wschat.sender');
	let roomId = localStorage.getItem('wschat.roomId');
	let messageContent = messageInput.value.trim();
	if (messageContent && stompClient) {
		let chatMessage = {
			roomId: roomId, sender: nickname, message: messageContent, type: 'TALK'
		};
		stompClient.send("/app/every-chat/message/"+ roomId, {}, JSON.stringify(chatMessage));
		messageInput.value = '';
	}
}

function alarmSubscribe() {
	let roomId = localStorage.getItem('wschat.roomId')
	let nickname = localStorage.getItem('wschat.sender');
	if (nickname != null && roomId != null) {
		start(nickname, roomId);
	}
}

function alarmMessage() {
	let nickname = localStorage.getItem('wschat.sender');
	let roomId = localStorage.getItem('wschat.roomId');
	//if ($("#sendButton").click) {
		fetch(`/room/publish?sender=${nickname}&roomId=${roomId}`);
	//}
}


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