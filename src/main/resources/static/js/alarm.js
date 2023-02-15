function start(username, roomId){
	console.log("username + roomId : "+ username + roomId)
	let id = username + "-" + roomId;
	const eventSource = new EventSource(`/room/subscribe/?id=${id}`);

	eventSource.onopen = (e) => {
	};
	eventSource.onerror = (e) => {
	};
	eventSource.onmessage = (e) => {
		let message = JSON.parse(e.data + "\n")
		if (message !== "" && message !== null && message !==undefined && message.sender !== "admin") {
			adminAlarmForm(message)
		}
		else if(message !== "" && message !== null && message !==undefined &&  message.sender === "admin" && roomId === message.roomId){
			alarmForm(message)
		}
	};
}
