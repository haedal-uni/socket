package com.dalcho.adme.config;

import com.dalcho.adme.config.security.JwtTokenProvider;
import com.dalcho.adme.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
//stomp

@RequiredArgsConstructor
@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {
	// client가 connect할 떄 header로 보낸 Authorization에 담긴 jwt Token을 검증
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
		// websocket 연결 시 header의 jwt token 검증
		if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
			log.info("websocket 연결 - jwt token 검증");
			//jwtTokenProvider.getUserFromToken(headerAccessor.getFirstNativeHeader("Authorization"));

			String accessToken = headerAccessor.getFirstNativeHeader("Authorization");
			User user = jwtTokenProvider.getUserFromToken(accessToken);
			if (user != null) {
				Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				throw new BadCredentialsException("Invalid JWT token");
			}
		}
		//throw new MessagingException("no permission! ");
		return message;
	}
}


/*
 *  전달된 JWT 토큰에서 사용자 정보를 가져와 User 객체를 반환
 *  그리고 preSend 메소드 내에서 이 User 객체를 사용하여 Authentication 객체를 생성하고, SecurityContextHolder에 인증 정보를 저장
 */