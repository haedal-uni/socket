package com.dalcho.adme.controller;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.ChatRoomMap;
import com.dalcho.adme.dto.EveryChatResponse;
import com.dalcho.adme.service.EveryChatServiceImpl;
import com.dalcho.adme.service.RedisService;
import com.dalcho.adme.util.ServletUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EveryChatController {
	private final EveryChatServiceImpl everyChatService;
	private final SimpMessagingTemplate template;
	private final RedisService redisService;
	private final Long hours = 10L;

	@GetMapping("/join")
	public DeferredResult<EveryChatResponse> everyRoomChat() {
		String sessionId = ServletUtil.getSession().getId();
		log.info(">> Join request. session id : {}", sessionId);

		final ChatRoomMap user = new ChatRoomMap(sessionId);
		final DeferredResult<EveryChatResponse> deferredResult = new DeferredResult<>(null);

		everyChatService.addUser(user, deferredResult);

		deferredResult.onCompletion(() -> everyChatService.cancelChatRoom(user));
		deferredResult.onError((throwable) -> everyChatService.cancelChatRoom(user));
		deferredResult.onTimeout(() -> everyChatService.timeout(user));

		return deferredResult;
	}

	@MessageMapping("/every-chat/message/{roomId}")
	public void sendsMessage(@DestinationVariable("roomId") String roomId, @Payload ChatMessage chatMessage) {
		log.info("Request message. roomd id : {} | chat message : {} | principal : {}", roomId, chatMessage);
		if (!StringUtils.hasText(chatMessage.getRoomId()) || chatMessage == null) {
			return;
		}

		if (chatMessage.getType() == ChatMessage.MessageType.TALK) {
			everyChatService.sendMessage(roomId, chatMessage);
		}
	}


	@MessageMapping("every-chat/addUser")
	public void everyChatAddUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor){
		redisService.addRedis(chatMessage, hours);
		String sessionId = (String) headerAccessor.getHeader("simpSessionId");
		everyChatService.connectUser(chatMessage.getRoomId(), sessionId);
		template.convertAndSend("/every-chat/" + chatMessage.getRoomId(), chatMessage);
	}
}
