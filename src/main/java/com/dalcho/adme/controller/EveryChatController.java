package com.dalcho.adme.controller;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.ChatRoomMap;
import com.dalcho.adme.dto.EveryChatResponse;
import com.dalcho.adme.service.EveryChatServiceImpl;
import com.dalcho.adme.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EveryChatController {
	private final EveryChatServiceImpl everyChatService;
	private final SimpMessagingTemplate template;
	private final RedisService redisService;

	@GetMapping("/join/{nickname}")
	public DeferredResult<EveryChatResponse> everyRoomChat(@PathVariable String nickname) { // session Id 대신 nickname으로 변경
		log.info(">> Join request. nickname : {}", nickname);
		final ChatRoomMap user = new ChatRoomMap(nickname);
		final DeferredResult<EveryChatResponse> deferredResult = new DeferredResult<>(null);

		everyChatService.addUser(user, deferredResult);

		deferredResult.onCompletion(() -> everyChatService.cancelChatRoom(user));
		deferredResult.onError((throwable) -> everyChatService.cancelChatRoom(user));
		deferredResult.onTimeout(() -> everyChatService.timeout(user));

		return deferredResult;
	}

	@GetMapping("/random/cancel/{nickname}")
	public DeferredResult<EveryChatResponse> abortChat(@PathVariable String nickname) {
		final ChatRoomMap user = new ChatRoomMap(nickname);
		DeferredResult<EveryChatResponse> deferredResult = new DeferredResult<>(null);
		everyChatService.cancelChatRoom(user);
		deferredResult.setErrorResult(new EveryChatResponse()); // 에러 결과 설정
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
		System.out.println(" = = = = = = = = =  RANDOM CHAT = = = = = = = = = = ");
		redisService.addRoomId(chatMessage);
		//String sessionId = (String) headerAccessor.getHeader("simpSessionId");
		everyChatService.connectUser(chatMessage.getRoomId(), chatMessage.getSender());
		template.convertAndSend("/every-chat/" + chatMessage.getRoomId(), chatMessage);
	}
}
