package com.dalcho.adme.controller;

import com.dalcho.adme.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.java.com.dalcho.adme.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {
	private final SimpMessageSendingOperations sendingOperations;
	private final ChatRepository chatService;

	@MessageMapping(value = "/chat/message")
	public void enter(ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("username", message.getSender());
		if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
			message.setMessage(message.getSender() + "님이 입장하였습니다.");
		}
		else if(ChatMessage.MessageType.QUIT.equals(message.getType())){
			message.setMessage(message.getSender() + "님이 퇴장하였습니다.");
		}
		sendingOperations.convertAndSend("/topic/chat/room/" + message.getRoomId(), message);
		// 메세지에 정의된 채널 id에 메세지를 보낸다.
		// /topic/chat/room/채널아이디 에 구독중인 클라이언트에게 메세지를 보낸다.
	}
}