package com.dalcho.adme.oauth2;

import com.dalcho.adme.exception.notfound.UserNotFoundException;
import com.dalcho.adme.model.User;
import com.dalcho.adme.oauth2.util.UserMapper;
import com.dalcho.adme.repository.UserRepository;
import com.dalcho.adme.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private final UserRepository userRepository;

	private final RedisService redisService;
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService  = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);
		String accessToken = userRequest.getAccessToken().getTokenValue();

		// OAuth2 서비스 id (구글, 카카오, 네이버)
		String registrationId = userRequest.getClientRegistration().getRegistrationId(); // kakao

		// OAuth2 로그인 진행 시 키가 되는 필드 값(PK)
		String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
				.getUserNameAttributeName(); // kakao는 id

		// OAuth2 로그인을 통해 가져온 OAuth2User의 attribute를 담아주는 of 메소드
		OAuth2Attribute oAuth2Attribute = OAuth2Attribute.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

		/*
		        if(provider.equals("google")){
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        }
        else if(provider.equals("naver")){
            oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
        }
        else if(provider.equals("kakao")){	//추가
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        }
		 */
		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();
		String nickname = random.ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				.limit(targetStringLength).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
		User user = userRepository.findByEmail(oAuth2Attribute.getEmail()).orElseGet(() -> {
			log.info("[db save] : social login");
			if (registrationId.equals("kakao")){
				User saved = UserMapper.ofKakao(oAuth2User, nickname);
				log.info("saved : " + saved);
				userRepository.save(saved);
				return saved;
			} else if (registrationId.equals("naver")) {
				User saved = UserMapper.ofNaver(oAuth2User, nickname);
				log.info("saved : " + saved);
				userRepository.save(saved);
				return saved;
			} else if(registrationId.equals("google")){
				User saved = UserMapper.ofGoogle(oAuth2User, nickname);
				log.info("saved : " + saved);
				userRepository.save(saved);
				return saved;
			}
			else{
				throw new OAuth2AuthenticationException("save error");
			}
		});
		if (!user.isEnabled()) throw new OAuth2AuthenticationException(new OAuth2Error("Not Found"), new UserNotFoundException());
		Map<String, Object> memberAttribute = oAuth2Attribute.convertToMap(); // {name=kakao에서 설정한 이름, id=email, key=email, email=test@kakao.com, picture=null}
		memberAttribute.put("id", user.getId());

		return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())), memberAttribute, "email");
	}
}

