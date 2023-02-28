package com.dalcho.adme.oauth2.util;

import com.dalcho.adme.model.Kakao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

	public static Kakao of(OAuth2User oAuth2User) {
		var attributes = oAuth2User.getAttributes();
		return Kakao.builder()
				.id((Long) attributes.get("id"))
				.email((String) attributes.get("email"))
				.password("")
				.nickname((String) attributes.get("sub"))
//                .picture((String)attributes.get("picture"))
				.build();
	}

	public static Kakao toEntity(OAuth2User oAuth2User, String passwrod) {
		var attributes = oAuth2User.getAttributes();
		return Kakao.builder()
				.id((Long) attributes.get("id"))
				.email((String) attributes.get("email"))
				.password(passwrod)
				.nickname((String) attributes.get("sub"))
//                .picture((String)attributes.get("picture"))
				.build();
	}
}
