package com.dalcho.adme.oauth2;

import com.dalcho.adme.model.Kakao;
import com.dalcho.adme.model.UserRole;
import com.dalcho.adme.config.security.JwtTokenProvider;
import com.dalcho.adme.oauth2.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component

public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
	private final JwtTokenProvider jwtProvider;
	@Value("${oauth.redirection.url}")
	private String REDIRECTION_URL;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws
			IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		Kakao kakao = UserMapper.of(oAuth2User); // kakao type으로 넣기
		String token = jwtProvider.generateToken(kakao.getNickname()); // string 으로 받는다
		response.sendRedirect(getRedirectionURI(token));
	}

	private String getRedirectionURI(String token) {
		return UriComponentsBuilder.fromUriString(REDIRECTION_URL).queryParam("token", token).build().toUriString();
	};
}


