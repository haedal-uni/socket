package com.dalcho.adme.controller;

import com.dalcho.adme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("nickname", "");
		return "index";
	}

	@GetMapping(value="/user/logout")
	public ModelAndView logout(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		userService.kakaoLogout((String)session.getAttribute("accessToken"));
		session.removeAttribute("accessToken");
		mav.setViewName("index");
		return mav;
	}
}