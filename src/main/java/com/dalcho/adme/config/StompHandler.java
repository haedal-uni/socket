package com.dalcho.adme.config;

import com.dalcho.adme.config.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

//stomp
@RequiredArgsConstructor
@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

		// websocket 연결 시 header의 jwt token 검증
		if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
			log.info("websocket 연결 - jwt token 검증");
			jwtTokenProvider.validateToken(headerAccessor.getFirstNativeHeader("Authorization"));
		}
		//throw new MessagingException("no permission! ");
		return message;
	}
}