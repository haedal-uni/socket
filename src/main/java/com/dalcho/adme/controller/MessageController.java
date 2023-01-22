package com.dalcho.adme.controller;


import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.service.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {
	private final SimpMessageSendingOperations sendingOperations;
	private final ChatServiceImpl chatService;
	private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

	//@MessageMapping("/chat/message") // 클라이언트에서 /app/chat/message로 메세지를 발행한다.
	@MessageMapping(value = "/chat/message")
	public void enter(ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("username", message.getSender());
		if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
			message.setMessage(message.getSender() + " 님이 입장하였습니다.");
		}
		sendingOperations.convertAndSend("/topic/chat/room/" + message.getRoomId(), message);
		// 메세지에 정의된 채널 id에 메세지를 보낸다.
		// /topic/chat/room/채널아이디 에 구독중인 클라이언트에게 메세지를 보낸다.
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String username = (String) headerAccessor.getSessionAttributes().get("username");
		if (username != null) {
			logger.info("User Disconnected : " + username);
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setType(ChatMessage.MessageType.QUIT);
			chatMessage.setSender(username);
			chatMessage.setMessage(chatMessage.getSender() + " 님이 퇴장하였습니다.");
			sendingOperations.convertAndSend("/topic/chat/room/" + chatMessage.getRoomId(), chatMessage.getMessage());
		}
	}
}