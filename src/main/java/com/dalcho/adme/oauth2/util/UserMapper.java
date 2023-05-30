package com.dalcho.adme.oauth2.util;

import com.dalcho.adme.model.User;
import com.dalcho.adme.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserMapper {
	public static User ofKakao(OAuth2User oAuth2User, String nickname) {
		log.info("ofKakao : " + oAuth2User);
		var attributes = oAuth2User.getAttributes();
		Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");
		Map<String, Object> properties = (Map<String, Object>) oAuth2User.getAttributes().get("properties");
		log.info("id : " + attributes.get("id"));
		return User.builder()
				.socialId(String.valueOf(attributes.get("id")))
				.email((String) kakao_account.get("email"))
				.password("")
				.username( (String) properties.get("nickname"))
				.nickname(nickname)
				.social("kakao")
//                .picture((String)attributes.get("picture"))
				.build();
	}

	public static User ofNaver(OAuth2User oAuth2User, String nickname) {
		log.info("ofNaver : " + oAuth2User);
		var attributes = oAuth2User.getAttributes();
		Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		return User.builder()
				.socialId((String) attributes.get("id"))
				.email((String) response.get("email"))
				.password("")
				.username((String) response.get("name"))
				.nickname(nickname)
				.social("naver")
				.build();
	}

	public static User ofGoogle(OAuth2User oAuth2User, String nickname) {
		log.info("ofGoogle : " + oAuth2User);
		var attributes = oAuth2User.getAttributes();

		return User.builder()
				.socialId((String) attributes.get("sub"))
				.email((String) attributes.get("email"))
				.password("")
				.username((String) attributes.get("name"))
				.nickname(nickname)
				.social("google")
				.build();
	}

	public static User of(OAuth2User oAuth2User, String nickname) {// nickname과 role만 있으면 됨
		var attributes = oAuth2User.getAttributes();
		var authority = oAuth2User.getAuthorities();
		//(String) attributes.get("name");
		String auth = authority.toString().replace("[","").replace("]","");
		return User.builder()
				//.email((String) attributes.get("email"))
				.password("")
				.nickname(nickname)
				.role(UserRole.of(auth))
//                .picture((String)attributes.get("picture"))
				.build();
	}
}
