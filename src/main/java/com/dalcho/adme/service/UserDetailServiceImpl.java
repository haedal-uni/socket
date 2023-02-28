package com.dalcho.adme.service;

import com.dalcho.adme.exception.notfound.KakaoNotFoundException;
import com.dalcho.adme.model.Kakao;
import com.dalcho.adme.repository.KakaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailServiceImpl implements UserDetailService {// @AuthenticationPrincipal에서 값을 받아오기 위해서는 아래 코드를 작성해야 한다.
//PrincipalDetailsService
	private final KakaoRepository kakaoRepository;

	// Kakao 엔티티의 id 값 가져오기 (인증)
	@Override
	public UserDetails loadUserByUsername(String email) {
		log.info("[loadUserByUsername] loadUserByUsername 수행. email : {}", email);
		return kakaoRepository.findByEmail(email).orElseThrow(() -> {
			throw new KakaoNotFoundException();
		});

	}
}
