package com.dalcho.adme.controller;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.service.ChatServiceImpl;
import com.dalcho.adme.service.EveryChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

//@EventListener는 동기적으로 처리를 진행
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {
	private final SimpMessageSendingOperations sendingOperations;
	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
	private final EveryChatServiceImpl everyChatService;
	private final ChatServiceImpl chatService;

	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {
		logger.info("Received a new web socket connection  ");
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) headerAccessor.getHeader("simpUser");
		String nickname = (String) token.getPrincipal().getAttributes().get("name");
		String role = token.getPrincipal().getAuthorities().toString().replace("[","").replace("]","");

		//String username = (String) headerAccessor.getSessionAttributes().get("username");

		String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
		if (roomId.startsWith("aaaa")) {
			logger.info("User Disconnected - random");
			String sessionId = (String) headerAccessor.getHeader("simpSessionId");
			everyChatService.disconnectUser(sessionId, roomId, nickname);
		} else {
			logger.info("User Disconnected : " + nickname);
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setType(ChatMessage.MessageType.LEAVE);
			chatMessage.setSender(nickname);
			chatMessage.setRoomId(roomId);
			chatService.connectUser("Disconnect", roomId);
			sendingOperations.convertAndSend("/topic/public/" + roomId, chatMessage);
		}
	}
}