package com.dalcho.adme.controller;

import com.dalcho.adme.config.security.JwtTokenProvider;
import com.dalcho.adme.exception.notfound.UserNotFoundException;
import com.dalcho.adme.model.User;
import com.dalcho.adme.repository.UserRepository;
import com.dalcho.adme.service.RedisService;
import com.dalcho.adme.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {
	private final UserService userService;
	private final RedisService redisService;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("nickname", "");
		return "index";
	}

	@GetMapping(value="/user/logout/{nickname}")
	public void logout(@PathVariable String nickname) {
		User byNickname = userRepository.findByNickname(nickname).orElseThrow(UserNotFoundException::new);
		log.info("redis getToken : {}",redisService.getToken(byNickname.getEmail()));
		String accessToken = redisService.getToken(byNickname.getEmail());
		userService.kakaoLogout(accessToken);
		redisService.deleteRedis(nickname);
	}
}