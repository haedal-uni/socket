package com.dalcho.adme.oauth2.util;

import com.dalcho.adme.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserMapper {

	public static User of(OAuth2User oAuth2User) {
		var attributes = oAuth2User.getAttributes();
		log.info("UserMapper의 of() " + attributes);
		return User.builder()
				.id((Long) attributes.get("id"))
				.email((String) attributes.get("email"))
				.password("")
				.nickname((String) attributes.get("sub"))
//                .picture((String)attributes.get("picture"))
				.build();
	}
}
