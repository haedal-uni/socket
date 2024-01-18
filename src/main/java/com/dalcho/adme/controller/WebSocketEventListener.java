package com.dalcho.adme.controller;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.exception.notfound.UserNotFoundException;
import com.dalcho.adme.repository.UserRepository;
import com.dalcho.adme.service.ChatServiceImpl;
import com.dalcho.adme.service.EveryChatServiceImpl;
import com.dalcho.adme.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static com.dalcho.adme.dto.ChatMessage.MessageType;

//@EventListener는 동기적으로 처리를 진행
@RequiredArgsConstructor
@Component
@Slf4j
public class WebSocketEventListener {
	private final SimpMessagingTemplate template;
	private final EveryChatServiceImpl everyChatService;
	private final ChatServiceImpl chatService;
	private final RedisService redisService;
	private final RedisTemplate<String, ChatMessage> redisTemplate;
	private final UserRepository userRepository;
	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {
		log.info("\"Received a new web socket connection  ");
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) headerAccessor.getHeader("simpUser");
		String nickname = token.getName();
		userRepository.findByNickname(nickname).orElseThrow(UserNotFoundException::new);
		String role = token.getAuthorities().toString().replace("[","").replace("]","");
		String roomId = redisService.getRedis(nickname);
		if (roomId.startsWith("aaaa")) {
			log.info("[랜덤 채팅] disconnected chat");
			everyChatService.disconnectUser(nickname, roomId);
		} else {
			log.info("[고객센터] disconnected chat - {} 의 roomId : {}", nickname, redisService.getRedis(nickname));
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setType(MessageType.LEAVE);
			chatMessage.setSender(nickname);
			chatMessage.setRoomId(roomId);
			chatService.connectUser("Disconnect", roomId, chatMessage);
			if (role.equals("ADMIN")){
				redisService.deleteRedis(nickname);
			}
			template.convertAndSend("/topic/public/" + roomId, chatMessage);
		}
	}
}