package com.dalcho.adme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpSession;

@Controller
public class HtmlController {
	// 채팅 리스트 화면
	@GetMapping("/room")
	public String rooms(Model model) {
		model.addAttribute("nickname", "");
		return "chat-list";
	}

	// 채팅방 입장 화면
	@GetMapping("/room/enter/{roomId}")
	public String roomDetail(Model model, @PathVariable String roomId) {
		model.addAttribute("roomId", roomId);
		return "chat-room";
	}

	@GetMapping("/every-chat")
	public String everyRoomDetail() {
		return "every-chat-room";
	}

	@GetMapping("/user/login")
	public String login(){
		return "login";
	}

	@GetMapping("/user/login/error")
	public String loginError(Model model){
		model.addAttribute("loginError", true);
		return "login";
	}

	// 회원 가입 페이지
	@GetMapping("/user/signup")
	public String signup() {
		return "signup";
	}
	@GetMapping("/error")
	public String error(){
		return "error";
	}

}
