let messageArea = document.querySelector('.body');
let username = localStorage.getItem('wschat.sender');
let chatArea = document.querySelector('.chat');

function openChat() {
	document.getElementById('container').classList.add('open');
	$.ajax({
		type: "POST", url: `/room/`, data: username, contentType: false, processData: false, success: function(response) {
			localStorage.setItem('wschat.roomName', username);
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
	if (message.sender == username) {
		divName = "user right"
	} else {
		divName = "user left"
	}
	let messageElement = document.createElement('div');
	if (message.type === 'JOIN') {
		getFile()
		if (message.sender != "admin") {
			messageElement.classList.add('event-message');
			message.message = message.sender + ' 님 안녕하세요';
		}
	} else if (message.type === 'LEAVE' && message.sender != "admin") {
		messageElement.classList.add('event-message');
		message.message = message.sender + ' 님이 나가셨습니다.';
	} else if (message.type === 'DELETE') {
		messageElement.classList.add('event-message');
		message.message = roomName + ' 님 채팅이 종료되어 ' + '현재 시간 [ ' + ms + ' ]  ' + ' 으로 부터 5분 뒤에 삭제될 예정입니다.';
	} else if (message.type === 'TALK' && message.message != null) {
		let avatarElement = $(".avatar");
		//avatarElement.style.backgroundColor = getAvatarColor(message.sender);
		//messageElement.appendChild(avatarElement);
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
	let roomId = localStorage.getItem('wschat.roomId')
	let roomName = username
	$.ajax({
		type: "GET", url: `/room/enter/` + roomId + '/' + roomName, contentType: false, processData: false, success: function(response) {
			console.log("response : " + response);
			for (let i = 0; i < response.length; i++) {
				onMessageReceived(response[i])
			}
		}
	});
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