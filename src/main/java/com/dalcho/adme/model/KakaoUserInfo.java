package com.dalcho.adme.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KakaoUserInfo {
	Long id;
	String email;
	String nickname;
}
