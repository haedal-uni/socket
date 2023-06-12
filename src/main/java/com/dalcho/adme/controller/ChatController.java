package com.dalcho.adme.controller;

import com.dalcho.adme.config.security.JwtTokenProvider;
import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.DisconnectPayload;
import com.dalcho.adme.model.User;
import com.dalcho.adme.service.ChatServiceImpl;
import com.dalcho.adme.service.EveryChatServiceImpl;
import com.dalcho.adme.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {
	private final SimpMessagingTemplate template;
	private final ChatServiceImpl chatService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisService redisService;
	private final EveryChatServiceImpl everyChatService;
	private final Long hours = 10L;
	/*
    websocket을 통해 서버에 메세지가 send 되었을 떄도 jwt token 유효성 검증이 필요하다.
    위와 같이 회원 대화명(id)를 조회하는 코드를 삽입하여 유효성이 체크될 수 있도록 한다.
   */
	@MessageMapping("/chat/sendMessage")
	public void sendMessage(@Payload ChatMessage chatMessage) {
		template.convertAndSend("/topic/public/" + chatMessage.getRoomId(), chatMessage);
	}

	@MessageMapping("/chat/addUser")
	public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
		String token = headerAccessor.getFirstNativeHeader("Authorization");
		User user= jwtTokenProvider.getUserFromToken(token);
		log.info("[chat] addUser token 검사: " + user.getNickname());
		chatMessage.setSender(user.getNickname());
		chatMessage.setType(ChatMessage.MessageType.JOIN);
		System.out.println(chatMessage.getRoomId());
		redisService.addRedis(chatMessage);
		chatService.connectUser("Connect", chatMessage.getRoomId(), chatMessage);
		template.convertAndSend("/topic/public/" + chatMessage.getRoomId(), chatMessage);
	}

	@MessageMapping("/chat/end-chat")
	public void endChat(@Payload ChatMessage chatMessage) {
		log.info("endchat");
		template.convertAndSend("/topic/public/" + chatMessage.getRoomId(), chatMessage);
	}

	// 일반 chat과 random chat 분리
	@MessageMapping("/disconnect")
	public void disConnect(@Payload DisconnectPayload disconnectPayload){
		String roomId = disconnectPayload.getRoomId();
		String nickname = disconnectPayload.getNickname();
		if (roomId.startsWith("aaaa")) {
			log.info("User Disconnected - random");
			everyChatService.disconnectUser(nickname, roomId);
		} else {
			log.info("User Disconnected : " + nickname);
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setType(ChatMessage.MessageType.LEAVE);
			chatMessage.setSender(nickname);
			chatMessage.setRoomId(roomId);
			chatService.connectUser("Disconnect", roomId, chatMessage);
			if (nickname.equals("admin")){
				redisService.deleteRedis(nickname);
			}
			template.convertAndSend("/topic/public/" + roomId, chatMessage);
		}

	}
}
