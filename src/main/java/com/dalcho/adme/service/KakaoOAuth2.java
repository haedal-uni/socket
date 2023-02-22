package com.dalcho.adme.service;

import com.dalcho.adme.model.KakaoUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;

@Component
@PropertySource("classpath:kakao.properties")
@Slf4j
public class KakaoOAuth2 {
	@Value("${api}")
	private String api;
	@Value("${secret}")
	private String secret_key;
	@Value("${redirect_url}")
	private String redirect_url;

	public KakaoUserInfo getUserInfo(String authorizedCode) { // 사용자 정보 응답 반환 method
		// 1. 인가코드 -> 액세스 토큰
		String accessToken = getAccessToken(authorizedCode);
		// 2. 액세스 토큰 -> 카카오 사용자 정보
		KakaoUserInfo userInfo = getUserInfoByToken(accessToken);
		return userInfo;
	}

	private String getAccessToken(String authorizedCode) { // 접근 토큰 반환 method
		// HttpHeader 오브젝트 생성
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		// 데이터를 key=value 형태로 만들어서 전달하라는 뜻

		// HttpBody 오브젝트 생성(4가지 고정 data , client_secret를 넣으면 보완 강화)
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", api); // rest api키
		params.add("redirect_uri", redirect_url); // 로그인 요청 콜백 주소
		params.add("code", authorizedCode); // 응답받는 code를 변수로 넣으면 된다.
		params.add("client_secret", secret_key);

		RestTemplate rt = new RestTemplate();

		// HttpHeader와 HttpBody를 하나의 오브젝트에 담기
		// 요청하기 위해서 헤더와 데이터(body)를 합친다. (kakaoTokenRequest는 데이터(Body)와 헤더(Header)를 Entity가 된다.)
		HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

		// Http 요청하기 - Post방식으로 - 그리고 response 변수의 응답 받음.
		//POST 방식이기 때문에 GET 방식처럼 쿼리스트링으로 전달하는 것이 아니라 http body에 데이터를 전달
		ResponseEntity<String> response = rt.exchange("https://kauth.kakao.com/oauth/token", // 요청할 서버주소(공식문서에서 지정)
				HttpMethod.POST, // 요청할 방식
				kakaoTokenRequest, // 요청할 때 보낼 데이터
				String.class // 요청 시 반환되는 데이터 타입
		);

		// JSON -> 액세스 토큰 파싱
		String tokenJson = response.getBody();
		JSONObject rjson = new JSONObject(tokenJson);
		String accessToken = rjson.getString("access_token");
		return accessToken;
		/*
AccessToken을 받았다는 것을 확인할 수 있다.
이 AccessToken을 통해 서버는 카카오쪽에 저장된 회원정보에 접근할 수 있는 권한이 생겼다.
		*/
	}

	//AccessToken으로 회원정보와 email을 가져온다.(GET / POST를 모두 지원한다)
	private KakaoUserInfo getUserInfoByToken(String accessToken) {
		log.info("getUserInfoByToken");
		// HttpHeader 오브젝트 생성
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		// HttpHeader와 HttpBody를 하나의 오브젝트에 담기
		RestTemplate rt = new RestTemplate();
		HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

		// Http 요청하기 - Post방식으로 - 그리고 response 변수의 응답 받음.
		ResponseEntity<String> response = rt.exchange("https://kapi.kakao.com/v2/user/me", // 요청주소
				HttpMethod.POST, kakaoProfileRequest, String.class);
		System.out.println("response : " + response);
		JSONObject body = new JSONObject(response.getBody());
		Long id = body.getLong("id");
		String email; // eamil 선택 사항이므로
		try {
			email = body.getJSONObject("kakao_account").getString("email");
		} catch (JSONException e) {
			email = "test@email.com";
		}
		String nickname = body.getJSONObject("properties").getString("nickname");
		//String image = body.getJSONObject("properties").getString("profile_image");
		return new KakaoUserInfo(id, email, nickname);
	}
}
