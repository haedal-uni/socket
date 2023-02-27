package com.dalcho.adme.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;
	private final String BEARER = "Bearer ";
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION);
		request.setAttribute("existsToken", true); // 토큰 존재 여부 초기화

		if (isEmptyToken(token)) request.setAttribute("existsToken", false); // 토큰이 없는 경우 false로 변경

		if (token == null || !token.startsWith(BEARER)) {
			filterChain.doFilter(request, response);
			return;
		}

		token = parseBearer(token);

		if (jwtTokenProvider.validateToken(token)) {
			Authentication authentication = jwtTokenProvider.getAuthentication(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	private boolean isEmptyToken(String token) {
		return token == null || "".equals(token);
	}

	private String parseBearer(String token) {
		return token.substring(BEARER.length());
	}
}