package com.dalcho.adme.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// servletRequest 에서 token 추출
		String token = jwtTokenProvider.resolveToken(request);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		log.info("[dofilterInternal] token 값 유효성 체크 시작");
		try {
			// token 이 유효하다면 Authentication 객체를 생성해서 SecurityContextHolder 에 추가
			if (token != null && jwtTokenProvider.validateToken(token)) {
				log.info("[ jwt ] Login");
				Authentication authentication = jwtTokenProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.info("[doFilterInternal] token 값 유효성 체크 완료");
			} else if (auth.getPrincipal() != null && token == null) {
				log.info(" [  oauth  ]   login ");
			} else {
				log.info("[doFilterInternal] 유효한 JWT 토큰이 없습니다, uri: {}", request.getRequestURL());
			}
		} catch (NullPointerException e) {
			log.info("doFilterInternal");
		} finally {
			// 요청 정보가 매칭되지 않을경우 동작
			filterChain.doFilter(request, response);
		}
	}
}