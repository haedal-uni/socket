package com.dalcho.adme.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// Stomp 설정
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler stompHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");// pub
        config.setPathMatcher(new AntPathMatcher(".")); // URL을 / → .으로
        //config.enableStompBrokerRelay( "/queue", "/exchange", "/amq/queue");
        config.enableStompBrokerRelay(  "/exchange");
        //config.enableSimpleBroker("/topic/ping", "/every-chat"); // sub
    }
	/*
	enableSimpleBroker를 통해 메시지 브로커가 /topic으로 시작하는 주소를 구독한 Subscriber들에게 메시지를 전달하도록 한다.
	setApplicationDestinationPrefixes는 클라이언트가 서버로 메시지를 발송할 수 있는 경로의 prefix를 지정한다.
	 */

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //소켓에 연결하기 위한 엔드 포인트를 지정
        registry.addEndpoint("/coco").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/coco/chat").setAllowedOriginPatterns("*").withSockJS();
    }

    // StompHandler가 Websocket 앞단에서 token을 체크할 수 있도록 interceptor로 설정
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // jwt 토큰 검증을 위해 생성한 stompHandler를 인터셉터로 지정해준다.
        registration.interceptors(stompHandler);
    }

}