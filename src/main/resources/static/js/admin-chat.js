let nickname = "admin";
let messageInput = document.querySelector(".write-message")
localStorage.setItem('wschat.sender', nickname);
let chatArea = document.querySelector('.messages-chat');
messageInput.addEventListener("keyup", function(event) {
	if (event.keyCode === 13) {
		event.preventDefault();
		document.getElementById("sendButton").click();
	}
});
$(document).ready(function() {
	findToken()
	chatList()
	$(".messages-chat").text("");
});
function findToken(){
	let urlSearch = new URLSearchParams(location.search);
	let token = urlSearch.get('token')
	if(token!=null && token !== localStorage.getItem('token')){
		localStorage.setItem('token', token);
		emptyUsername(token)
	}
}
function chatList(){
	$.ajax({
		type: "GET", url: `/rooms`, contentType: false, processData: false, success: function(response) {
			console.log("채팅방 불러오기 (all) : " + JSON.stringify(response))
			for (let i = 0; i < response.length; i++) {
				let nickname = response[i]["nickname"];
				let roomId = response[i]["roomId"];
				let roomName = nickname + " 님의 채팅방";
				let roomNum = "'" + roomId + "'"
				let tempHtml = `
                         <div id=${nickname} class="discussion" style="display:none" onclick="enterRoom(${roomNum})">
        <div class="photo" style="background-color: #82D1E3;">
        </div>
        <div class="desc-contact">
          <p class="name">${nickname}</p>
          <p class="message">Let's meet for a coffee or something today ?</p>
          <div class=${roomId} style="display: none">${nickname}</div>
        </div>
      </div>
`
				$(".discussions").append(tempHtml);
			}
		}
	})
}

function enterRoom(roomId) {
	let roomName = document.getElementsByClassName(roomId)[0].textContent;
	localStorage.setItem('wschat.roomName', roomName);
	localStorage.setItem('wschat.roomId', roomId);
	$(".adme-name").text(roomName);

	let temp_html=`
	<button onclick="joinChat()">yes</button>
	<button onclick="reset()">no</button>
	`
	$(".messages-chat").append(temp_html)
}
function reset(){
	$(".chat").text("");
}
function joinChat(){
	$(".messages-chat").text("")
	let roomName = localStorage.getItem('wschat.roomName')
	// let idName = "#" + roomName
	// $(idName).text("")
	getFile()
	connect()
}
function leaveChat(){
	let roomName = localStorage.getItem('wschat.roomName')
	let idName = "#" + roomName
	$(idName).css('display','none');
	stompClient.disconnect()
}

function onMessageReceived(payload) { // 메세지 받기
	let roomName = localStorage.getItem('wschat.roomName')
	let message;
	try {
		message = JSON.parse(payload.body);
	} catch (SyntaxError) {
		message = payload;
	}
	let divName = "";
	if (message.sender != "admin") {
		divName = "text"
	} else {
		divName = "response"
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
         <div class="message text-only">
          <p class="${divName}">${message.message}</p>
        </div>
		`
		$(".messages-chat").append(temp)
		chatArea.scrollTop = chatArea.scrollHeight;
	}
}

function connect() {
	let token= localStorage.getItem('token')
	let nickname = localStorage.getItem('wschat.sender');
	if (nickname) {
		let socket = new SockJS('/ws');
		stompClient = Stomp.over(socket);
		stompClient.connect({Authorization: token}, onConnected, onError);
	}
}

function onConnected() {
	let token= localStorage.getItem('token')
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
	let roomName = localStorage.getItem('wschat.roomName')
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
	let roomName = localStorage.getItem('wschat.roomName')
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

function alarmSubscribe() {
	let roomId = localStorage.getItem('wschat.roomId')
	let nickname = localStorage.getItem('wschat.sender');
	if (nickname != null && roomId != null) {
		start(nickname, roomId);
	}
}

function closeAlarm() {
	$('.toast').toast('hide')
}

function adminAlarmForm(data) {
	let idName = "#" + data.sender
	$(idName).css('display','block');
	if ($(".toast-body").text().split(" ")[0] !== data.sender) {
		let toast = "<div class='toast' role='alert' aria-live='assertive' aria-atomic='true'>";
		toast += "<div class='toast-header'><i class='fas fa-bell mr-2'></i><strong class='mr-auto'>알림</strong>";
		toast += "<small class='text-muted'>just now</small><button type='button' class='ml-2 mb-1 close' data-dismiss='toast' aria-label='Close'>";
		toast += "<span aria-hidden='true'>&times;</span></button>";
		toast += "</div> <div class='toast-body'>" + data.message + "</div></div>";
		$("#msgStack").append(toast);   // msgStack div에 생성한 toast 추가
		$(".toast").toast({"animation": true, "autohide": false});
		$('.toast').toast('show');
	}
	if ($('.toast').toast('hide')) {
		$('.toast').toast('show')
	}
	if ($(".toast fade hide show")) {
		$('.toast').toast('show')
	}
}