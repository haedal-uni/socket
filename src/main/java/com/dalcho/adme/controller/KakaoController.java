package com.dalcho.adme.controller;

import com.dalcho.adme.service.KaKaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.net.URI;

@Controller
@RequiredArgsConstructor
public class KakaoController {
	private final KaKaoService kaKaoService;

	@GetMapping("/user/kakao/callback")
	@ResponseBody
	public ResponseEntity<?> kakaoLogin(String code, HttpSession httpSession) {
		// authorizedCode: 카카오 서버로부터 받은 인가 코드
		kaKaoService.kakaoLogin(code, httpSession); // 토큰 발급 요청
		System.out.println();

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(URI.create("/room"));
		return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY); // 카카오 인증 완료
	}

	//@AuthenticationPrincipal null로 뜨뮤ㅠ
	@GetMapping("/")
	public String home(Model model, HttpSession httpSession) {
		model.addAttribute("username", httpSession.getAttribute("username"));
		return "index";
	}

}