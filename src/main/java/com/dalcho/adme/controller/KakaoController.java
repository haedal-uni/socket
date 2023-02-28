package com.dalcho.adme.controller;

import com.dalcho.adme.model.KakaoUserInfo;
import com.dalcho.adme.service.KaKaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class KakaoController {
	private final KaKaoService kaKaoService;

	@GetMapping("/")
	public String home(Model model, HttpSession httpSession, @AuthenticationPrincipal KakaoUserInfo kakaoUserInfo) {
		model.addAttribute("nickname", httpSession.getAttribute("nickname"));
		return "index";
	}

	@GetMapping(value="/logout")
	public ModelAndView logout(HttpSession session) {
		ModelAndView mav = new ModelAndView();

		kaKaoService.kakaoLogout((String)session.getAttribute("accessToken"));
		session.removeAttribute("accessToken");
		session.removeAttribute("username");
		mav.setViewName("index");
		return mav;
	}
}