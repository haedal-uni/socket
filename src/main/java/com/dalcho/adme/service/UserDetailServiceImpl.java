package com.dalcho.adme.service;

import com.dalcho.adme.model.User;
import com.dalcho.adme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailServiceImpl implements UserDetailService {// @AuthenticationPrincipal에서 값을 받아오기 위해서는 아래 코드를 작성해야 한다.
//PrincipalDetailsService
	private final UserRepository userRepository;

	// Kakao 엔티티의 id 값 가져오기 (인증)
	@Override
	public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
		return userRepository.findByNickname(nickname)
				.map(this::buildUserDetails)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + nickname));
	}

	private UserDetails buildUserDetails(User user) {
		List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
		return new org.springframework.security.core.userdetails.User(
				user.getNickname(),
				user.getPassword(),
				authorities
		);
	}
}
