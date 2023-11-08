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
let status = ""
let currentXHR;
let timerInterval;
const unsentMessages = [];
let messageToSend = null;
let today = new Date();
let month = today.getMonth()+1;
let days = today.getDate();

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
	if (document.getElementById("needChat")) {
		//needLine()
	}else{
		let nickname = localStorage.getItem('wschat.sender');
		$.ajax({
			type: "POST",
			url: `/room`,
			data: nickname,
			contentType: false,
			processData: false,
			success: function(response) {
				localStorage.setItem('wschat.roomName', nickname);
				localStorage.setItem('wschat.roomId', response["roomId"]);
				let count = response["userChat"];
				let message = response["message"];
				let day = response["day"]
				let time = response["time"]
				today = new Date();
				month = today.getMonth()+1;
				days = today.getDate();
				let now = month + "/" + days
				let dayTime
				if(now!==day){
					dayTime = day + " " + time;
				}else{
					dayTime = time;
				}
				let temp = `
          <div id="needChat" class="conversation" onclick="joinChat()">
            <div class="top">
              <span class="badge">${count}</span>
              <span class="title">리스트에서 제목</span>
              <span class="time">${dayTime}</span>
            </div>
            <div class="bottom">
              <span class="user">${nickname}</span>
              <span class="message">${message}</span>
            </div>
          </div>
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
				$(".conversations").append(temp);
			}
		});
	}
}
function needLine(){
	$(".conversations").empty();
	roomId = localStorage.getItem('wschat.roomId');
	nickname = localStorage.getItem('wschat.sender');
	$.ajax({
		type: "GET",
		url: `/room/enter/`+ roomId,
		contentType: false,
		processData: false,
		success: function(response) {
			let count = response[1];
			let message = response[2];
			let day = response[3];
			let time = response[4];
			today = new Date();
			month = today.getMonth()+1;
			days = today.getDate();
			let now = month + "/" + days;
			let dayTime
			if(now !== day){
				dayTime = day + " " + time;
			}else{
				dayTime = time;
			}

			let temp = `
          <div id="needChat" class="conversation" onclick="joinChat()">
            <div class="top">
              <span class="badge">${count}</span>
              <span class="title">리스트에서 제목</span>
              <span class="time">${dayTime}</span>
            </div>
            <div class="bottom">
              <span class="user">${nickname}</span>
              <span class="message">${message}</span>
            </div>
          </div>
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
			$(".conversations").append(temp);
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
	if (message.sender !== "admin") {
		divName = "user right"
	} else {
		divName = "user left"
	}
	if (message.type === 'JOIN') {
		if (message.sender !== "admin") {
			alarmCount(0);
			message.message = message.sender + ' 님 안녕하세요';
			seperator(message.message);
		}
	} else if (message.type === 'LEAVE' && message.sender !== "admin") {
		message.message = message.sender + ' 님이 나가셨습니다.';
		seperator(message.message);
	} else if (message.type === 'DELETE') {
		message.message = roomName + ' 님 채팅이 종료되어 ' + '현재 시간 [ ' + ms + ' ]  ' + ' 으로 부터 5분 뒤에 삭제될 예정입니다.';
		seperator(message.message);
	} else if (message.type === 'TALK' && message.message != null) {
		today = new Date();
		month = today.getMonth()+1;
		days = today.getDate();
		let dd = month + "/" + days;
		let dayTime
		if(dd !== message.day){
			dayTime = message.day + " " + message.time;
		}else{
			dayTime = message.time;
		}
		let temp = `
    <div class="${divName}">
    <i class = "avatar">${message.sender[0]}</i>
        <div class="messages">
            <div class="message">
                <div class="message-container">
                    <div class="message-sender">&#9989; _ ${message.sender}</div>
                    <p class="text">${message.message}</p>
                </div>
            <div class="message-time">${dayTime}</div>
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
	if(status==="randomChat"){
		//randomChat()
		clearInterval(timerInterval); // 타이머 중지
		connectingElement.text("")
		$.ajax({
			type: "GET",
			url: "/random/cancel/" + nickname,
			success: function() {
				// 취소에 성공한 경우
			},
			error: function() {
				// 취소에 실패한 경우
			}
		});
	}
	status = ""
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
		//stompClient.send('/app/disconnect', {}, JSON.stringify({ roomId: roomId, nickname:nickname }));
		stompClient.disconnect();
		$("#sendButton").css("display","none")
	}
}

function backChat() {
	isRun = false;
	$(".body").text("")
	if(stompClient==null){
		$("#randomSendButton").css("display","none")
	} else if(stompClient.connect()){
		//stompClient.send('/app/disconnect', {}, JSON.stringify({ roomId: roomId, nickname:nickname }));
		stompClient.disconnect();
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
	$("#sendButtonType").empty();
	let temp = `
	<button class="btn btn-round btn-icon" id="sendButton" type="button" onclick="sendMessage()">send
<i class="fa fa-paper-plane"></i>
</button>
	`
	$("#sendButtonType").append(temp)
}
let socket
function connect() {
	status = "question"
	let nickname = localStorage.getItem('wschat.sender');
	let token = localStorage.getItem('token');
	if (nickname) {
		socket = new SockJS('/ws');
		stompClient = Stomp.over(socket);
		stompClient.connect({Authorization: token}, onConnected, onError);
	}
}

function onConnected() {
	let token = localStorage.getItem('token');
	roomId = localStorage.getItem('wschat.roomId')
	stompClient.subscribe('/topic/public/' + roomId, onMessageReceived);

	let message = "";
	if (document.querySelector('.message-container')) {
		message = $(".message-container").last().text().trim().split("\n")[1].trim()
	}
	stompClient.send("/app/chat/addUser", {Authorization: token}, JSON.stringify({roomId: roomId, type: 'JOIN', message: message}))
	//(void) send(destination, headers = {}, body = '')

	// //redis로 응답 받기
	// socket.onmessage = (event) => {
	// 	const message = event.data;
	// 	console.log('받은 메시지:', message)
	// 	onMessageReceived(message)
	// };
}

function onError(error) {
	// if (connectingElement) {
	// 	connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
	// 	connectingElement.style.color = 'red';
	// }
	stompClient = null;
	let temp = `
<div class="seperator">
    <div class="text-warning" style="color: red; font-size: 15px">채팅을 이용할 수 없습니다.<br />서버 연결을 확인해주세요.</div>
</div>`
	$(".body").append(temp);
	if (messageToSend) {
		unsentMessages.push(messageToSend);
		console.log("messageToSend : " + messageToSend);
	}
	/*
	{"roomId":"111","type":"TALK","sender":"nickname","message":"11"}
	이런 형식으로 저장해야됨
	 */
}

// 메세지 보내기
function sendMessage() {
	alarmMessage()
	let nickname = localStorage.getItem('wschat.sender');
	roomId = localStorage.getItem('wschat.roomId');
	let messageContent = messageInput.value.trim();
	today = new Date();
	month = today.getMonth()+1;
	days = today.getDate();
	let hour = ('0' + today.getHours()).slice(-2);
	let minute = ('0' + today.getMinutes()).slice(-2);

	let chatMessage = {
		roomId: roomId,
		sender: nickname,
		message: messageContent,
		type: 'TALK',
		day : month + "/" + days,
		time : hour + ":" + minute
	};
	if (messageContent && stompClient) {
		saveFile(chatMessage)
		stompClient.send("/app/chat/sendMessage", {}, JSON.stringify(chatMessage));
		messageInput.value = '';
	}else{
		messageToSend = chatMessage;
	}
}

function saveFile(chatMessage) {
	if(stompClient){
		roomId = localStorage.getItem('wschat.roomId');
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
}

let isRun = false;

function getFile() {
	roomId = localStorage.getItem('wschat.roomId');
	if (isRun == true) {
		return;
	}
	//isRun = true;
	$.ajax({
		type: "GET",
		url: `/room/enter/` + roomId + '/' + roomName,
		contentType: false,
		processData: false,
		success: function(response) {
			for (let i = 0; i < response.length; i++) {
				onMessageReceived(response[i])
			}
		},error: function(jqXHR, textStatus, errorThrown) {
			$(".body").text(`안녕하세요 ${nickname} 님 고객센터 입니다.`);
		}
	})
}
function timer(){
	let time = 20;
	let min = "";
	let sec = "";
	clearInterval(timerInterval); // 이전 인터벌 제거
	timerInterval = setInterval(function(){
		min = parseInt(time/60);
		sec = time%60;
		connectingElement.text("  현재 1명의 접속을 기다리고 있는 중입니다.  [ " + min + " 분  " + sec + " 초 ]");
		time--;
		if (time < 0) {
			clearInterval(timerInterval);
			connectingElement.text("시간 초과 다시 시도해주세요");
		}
	}, 1000)
}
function randomChat() {
	status = "randomChat"
	document.querySelector('.chat').classList.remove('close');
	let textBox = document.getElementById('message-input');
	textBox.disabled = true;

	// 이전 통신을 취소
	if (currentXHR) {
		currentXHR.abort();
	}

	currentXHR = $.ajax({
		type: "GET",
		url: `/join/`+ nickname,
		contentType: 'application/json',
		async:true,
		processData: false,
		beforeSend: function() {
			connectingElement.text("다른 user가 접속할 때 까지 대기중입니다.")
			timer();
			joinInterval = setInterval(function() {

				//connectingElement.text("현재 접속을 기다리고 있는 중입니다.")
			}, );
		},
		success: function(chatMessage) {
			response = JSON.stringify(chatMessage)
			clearInterval(joinInterval);
			status = ""
			if (!response){
				return;
			}
			let message = JSON.parse(response);
			if (message.type === 'SUCCESS') {
				textBox.disabled = false;
				connectingElement.text("모두 접속하여 채팅방이 open 되었습니다.");
				//sessionId = message.sessionId;
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
	roomId = localStorage.getItem('wschat.roomId');
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
	roomId = localStorage.getItem('wschat.roomId')
	let nickname = localStorage.getItem('wschat.sender');
	if (nickname != null && roomId != null && stompClient) {
		start(nickname, roomId);
	}
}

function alarmMessage() {
	if(stompClient){
		let nickname = localStorage.getItem('wschat.sender');
		roomId = localStorage.getItem('wschat.roomId');
		//if ($("#sendButton").click) {
		fetch(`/room/publish?sender=${nickname}&roomId=${roomId}`);
		//}
	}
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