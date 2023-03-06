package com.dalcho.adme.controller;

import com.dalcho.adme.config.security.JwtTokenProvider;
import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.user.LoginInfo;
import com.dalcho.adme.service.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ChatController {
	private final SimpMessagingTemplate template;
	private final ChatServiceImpl chatService;
	private final JwtTokenProvider jwtTokenProvider;

	/*
    websocket을 통해 서버에 메세지가 send 되었을 떄도 jwt token 유효성 검증이 필요하다.
    위와 같이 회원 대화명(id)를 조회하는 코드를 삽입하여 유효성이 체크될 수 있도록 한다.
   */
	@MessageMapping("/chat/sendMessage")
	public void sendMessage(@Payload ChatMessage chatMessage, @Header("Authorization") String token) {
		String nickname = jwtTokenProvider.getUserNameFromJwt(token);

		System.out.println("nickname " + nickname);
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

	@MessageMapping("/chat/user")
	public LoginInfo getUserInfo() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String name = authentication.getName();
		return LoginInfo.builder().name(name).token(jwtTokenProvider.generateToken(name)).build();
	}
}
