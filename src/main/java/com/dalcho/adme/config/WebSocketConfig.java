package com.dalcho.adme.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration // 컨테이너 등록
@EnableWebSocketMessageBroker // 웹소켓 서버 사용 설정(메시지 브로커가 지원하는 WebSocket 메시지 처리를 활성화)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) { // Stomp 사용을 위한 Message Broker 설정을 해주는 메소드
		config.enableSimpleBroker("/queue", "/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	// connection을 맺을때 CORS 허용
	public void registerStompEndpoints(StompEndpointRegistry registry) { // addEndpoint() : 소켓 연결 uri
		// endpoint는 양 사용자 간 웹소켓 핸드 셰이크를 위해 지정
		registry.addEndpoint("/ws/chat").setAllowedOriginPatterns("*").withSockJS();
	}
}