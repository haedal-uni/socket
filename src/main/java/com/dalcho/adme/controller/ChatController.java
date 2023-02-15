package com.dalcho.adme.controller;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.service.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
	private final SimpMessagingTemplate template;
	private final ChatServiceImpl chatService;

	@MessageMapping("/chat/sendMessage")
	public void sendMessage(@Payload ChatMessage chatMessage) {
		template.convertAndSend("/topic/public/" + chatMessage.getRoomId(), chatMessage);
	}

	@MessageMapping("/chat/addUser")
	public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
		//socket session 에 sender, roomId 저장
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		headerAccessor.getSessionAttributes().put("roomId", chatMessage.getRoomId());
		chatService.connectUser("Connect", chatMessage.getRoomId());
		template.convertAndSend("/topic/public/" + chatMessage.getRoomId(), chatMessage);
	}
}
