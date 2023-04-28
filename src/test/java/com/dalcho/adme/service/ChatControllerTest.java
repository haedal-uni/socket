package com.dalcho.adme.service;

import com.dalcho.adme.controller.ChatController;
import com.dalcho.adme.dto.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

//@ExtendWith(MockitoExtension.class)
class ChatControllerTest { // server -> client
	@Mock
	private SimpMessagingTemplate template;
	// ChatController의 sendMessage() 메소드에서 실제로 template.convertAndSend()가 호출되는지 확인하기 위해서
	//SimpMessagingTemplate 인스턴스를 Mock 객체로 생성

	@InjectMocks
	private ChatController chatController;

	@BeforeEach
	void setUp() { // @ExtendWith(MockitoExtension.class)를 class에 붙이거나 요골 쓰거나
		// @Mock 어노테이션으로 선언된 필드들을 초기화 하기 위해서는 MockitoAnnotations 클래스의 openMocks() 메소드를 이용해야함
		//이 코드는 모든 @Mock 어노테이션으로 선언된 필드를 초기화 하기 위해 작성
		MockitoAnnotations.openMocks(this);
		// this : ChatControllerTest 클래스의 인스턴스
		//@Mock 어노테이션을 통해 선언한 SimpMessagingTemplate 객체를 Mockito에서 제공하는 mock 객체로 만들기 위해서는 this 인스턴스를 전달

	}

	@Test
	void sendMessageTest() {
		// given
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setType(ChatMessage.MessageType.TALK);
		chatMessage.setMessage("message");
		chatMessage.setSender("sender");
		chatMessage.setRoomId("roomId");

		//( client -> server 없어도 되는 코드)
		/*
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SEND);
		headerAccessor.setDestination("/app/chat/sendMessage");
		headerAccessor.setSessionId("session1"); //StompSession을 생성하면 내부에서는 자동으로 sessionId를 생성
		headerAccessor.setSessionAttributes(new HashMap<>());
*/
		// when
		chatController.sendMessage(chatMessage);

		// then
		verify(template).convertAndSend(eq("/topic/public/" + chatMessage.getRoomId() ), eq(chatMessage));
		// verify : 특정 객체가 특정 메소드를 특정 매개 변수로 호출되는지 확인
		// verify(template) : SimpMessagingTemplate 객체가 제공하는 메소드 중 하나가 호출되었는지 검증하는 용도
		// eq : Mockito에서 제공하는 메소드로, 객체를 비교할 때 사용
	}
}

