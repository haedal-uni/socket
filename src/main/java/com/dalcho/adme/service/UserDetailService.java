package com.dalcho.adme.service;

import com.dalcho.adme.exception.notfound.KakaoNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserDetailService extends UserDetailsService {
	UserDetails loadUserByUsername(String nickname) throws KakaoNotFoundException;
}
