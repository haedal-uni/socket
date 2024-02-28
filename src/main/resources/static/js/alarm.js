function start(username, roomId){
	const eventSource = new EventSource(`/alarm/subscribe/${username}`);
	eventSource.onopen = (e) => {
	};
	eventSource.onerror = (e) => {
		if (e.readyState == EventSource.CLOSED) {
			// Connection was closed.
			// 종료 시 할 일
		}
		// 종료 또는 에러 발생 시 할 일
		eventSource.close();
	};
	eventSource.onmessage = (e) => {
		let message = JSON.parse(e.data + "\n")
		if(message !== "" && message !== null && message !==undefined &&  message.sender === "admin" && roomId === message.roomId){
			alarmForm(message)
		}
	};
}


function alarmForm(data) {
	alarmCount(1);
	if ($(".toast-body").text() === "") {
		let toast = "<div class='toast' role='alert' aria-live='assertive' aria-atomic='true'>";
		toast += "<div class='toast-header'><i class='fas fa-bell mr-2'></i><strong class='mr-auto'>알림  </strong>";
		toast += "<small class='text-muted'>just now  </small><button type='button' onclick='closeAlarm()' class='ml-2 mb-1 close' data-dismiss='toast' aria-label='Close'>";
		toast += "<span aria-hidden='true'>&times;</span></button>";
		toast += "</div> <div class='toast-body'>" + data.message + "</div></div>";
		$("#msgStack").append(toast);
		setTimeout(function() {
			$("#msgStack").hide()
		}, 2000);
	}
	if($("#msgStack").css("display") === "none"){
		$("#msgStack").show();
		setTimeout(function() {
			$("#msgStack").hide()
		}, 2000);
	}
}

function closeAlarm() {
	$("#msgStack").hide()
}
