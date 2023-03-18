package com.dalcho.adme.controller;

import com.dalcho.adme.config.security.JwtTokenProvider;
import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.service.ChatServiceImpl;
import com.dalcho.adme.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
	public void addUser(@Payload ChatMessage chatMessage, @Header("Authorization") String token) {
		String nickname = jwtTokenProvider.getNickname(token);
		chatMessage.setSender(nickname);
		redisService.addRedis(chatMessage, hours);
		log.info("redis roomId : {}", redisService.getRedis(chatMessage.getSender()));
		chatService.connectUser("Connect", chatMessage.getRoomId());
		template.convertAndSend("/topic/public/" + chatMessage.getRoomId(), chatMessage);
	}

	@MessageMapping("/chat/end-chat")
	public void endChat(@Payload ChatMessage chatMessage) {
		log.info("endchat");
		template.convertAndSend("/topic/public/" + chatMessage.getRoomId(), chatMessage);
	}
}
