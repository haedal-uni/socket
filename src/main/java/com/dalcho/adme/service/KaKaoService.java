package com.dalcho.adme.service;

import com.dalcho.adme.model.Kakao;
import com.dalcho.adme.model.KakaoUserInfo;
import com.dalcho.adme.model.UserRole;
import com.dalcho.adme.repository.KakaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class KaKaoService {
	private final KakaoOAuth2 kakaoOAuth2;
	private final KakaoRepository kakaoRepository;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;

	public void kakaoLogin(String authorizedCode, HttpSession httpSession) {
		// 카카오 OAuth2 를 통해 카카오 사용자 정보 조회
		KakaoUserInfo userInfo = kakaoOAuth2.getUserInfo(authorizedCode); 	// id, email, nickname

		Long kakaoIdx = userInfo.getId();
		String nickname = userInfo.getNickname();
		String email = userInfo.getEmail();
		// 우리 DB 에서 회원 Id 와 패스워드
		// 회원 Id = 카카오 nickname
		String username = nickname;

		// 랜덤 숫자 알파벳
		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();
		String generatedString = random.ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				.limit(targetStringLength).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
		String password = generatedString;

		// DB 에 중복된 Kakao Id 가 있는지 확인
		Kakao kakaoUser = kakaoRepository.findByKakaoIdx(kakaoIdx).orElse(null);

		// 카카오 정보로 회원가입(DB에 없는 사용자라면 회원가입처리)
		if (kakaoUser == null) {
			log.info("회원가입 처리");
			// 패스워드 인코딩
			String encodedPassword = passwordEncoder.encode(password);
			// ROLE = 사용자
			UserRole role = UserRole.USER;
			kakaoUser = new Kakao(nickname, encodedPassword, email, role, kakaoIdx);
			kakaoRepository.save(kakaoUser);
		}

		log.info("로그인 처리");
		// 로그인 처리
		httpSession.setAttribute("username", username);
		UsernamePasswordAuthenticationToken kakaoUsernamePassword = new UsernamePasswordAuthenticationToken(username, password);
		//kakaoUsernamePassword.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		//kakaoUsernamePassword.setDetails(kakaoUser);
		Authentication authentication = authenticationManager.authenticate(kakaoUsernamePassword);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
