package com.dalcho.adme.oauth2.util;

import com.dalcho.adme.model.User;
import com.dalcho.adme.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserMapper {
	public static User ofKakao(OAuth2User oAuth2User) {
		log.info("ofKakao : " + oAuth2User);
		var attributes = oAuth2User.getAttributes();
		Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");
		Map<String, Object> properties = (Map<String, Object>) oAuth2User.getAttributes().get("properties");
		return User.builder()
				.kakaoId((Long) attributes.get("id"))
				.email((String) kakao_account.get("email"))
				.password("")
				.nickname( (String) properties.get("nickname"))
//                .picture((String)attributes.get("picture"))
				.build();
	}

	public static User of(OAuth2User oAuth2User) {// nickname과 role만 있으면 됨
		var attributes = oAuth2User.getAttributes();
		var authority = oAuth2User.getAuthorities();

		String auth = authority.toString().replace("[","").replace("]","");
		return User.builder()
				//.email((String) attributes.get("email"))
				.password("")
				.nickname( (String) attributes.get("name"))
				.role(UserRole.of(auth))
//                .picture((String)attributes.get("picture"))
				.build();
	}
}
