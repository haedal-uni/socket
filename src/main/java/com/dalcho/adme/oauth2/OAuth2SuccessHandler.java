package com.dalcho.adme.oauth2;

import com.dalcho.adme.exception.notfound.UserNotFoundException;
import com.dalcho.adme.model.User;
import com.dalcho.adme.config.security.JwtTokenProvider;
import com.dalcho.adme.model.UserRole;
import com.dalcho.adme.oauth2.util.UserMapper;
import com.dalcho.adme.repository.UserRepository;
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
	private final UserRepository userRepository;
	@Value("${oauth.redirection.url}")
	private String REDIRECTION_URL;
	@Value("${oauth.admin.redirection.url}")
	private String ADMIN_REDIRECTION_URL;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws
			IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		String email = (String) oAuth2User.getAttributes().get("email");
		User userInfo = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
		String nickname = userInfo.getNickname();
		User user = UserMapper.of(oAuth2User, nickname); // kakao type으로 넣기
		String token = jwtProvider.generateToken(user); // string 으로 받는다

		response.sendRedirect(getRedirectionURI(token, user));
	}

	private String getRedirectionURI(String token, User user) {
		if(user.getRole().name().equals(UserRole.USER.name())){
			return UriComponentsBuilder.fromUriString(REDIRECTION_URL).queryParam("token", token).build().toUriString();
		}else if (user.getRole().name().equals(UserRole.ADMIN.name())){
			return UriComponentsBuilder.fromUriString(ADMIN_REDIRECTION_URL).queryParam("token", token).build().toUriString();
		}else{
			throw new RuntimeException("[error] 잘못된 권한입니다.");
		}
	};
}


