package com.dalcho.adme.service;

import com.dalcho.adme.config.security.JwtTokenProvider;
import com.dalcho.adme.model.Kakao;
import com.dalcho.adme.model.KakaoUserInfo;
import com.dalcho.adme.model.UserRole;
import com.dalcho.adme.repository.KakaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class KaKaoService {
	public void kakaoLogout(String accessToken) {
		String reqURL = "http://kapi.kakao.com/v1/user/logout";
		try {
			URL url = new URL(reqURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode = " + responseCode);
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String result = "";
			String line = "";
			while ((line = br.readLine()) != null) {
				result += line;
			}
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
