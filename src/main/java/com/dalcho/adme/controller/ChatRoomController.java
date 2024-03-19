package com.dalcho.adme.controller;

import com.dalcho.adme.config.security.JwtTokenProvider;
import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.ChatRoomDto;
import com.dalcho.adme.dto.LastMessage;
import com.dalcho.adme.service.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {
	private final ChatServiceImpl chatService;
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
	private static final Map<String, SseEmitter> CLIENTS = new ConcurrentHashMap<>();
	private final JwtTokenProvider jwtTokenProvider;

	// 모든 채팅방 목록 반환(관리자)
	@GetMapping("/rooms")
	public List<ChatRoomDto> room() {
		return chatService.findAllRoom();
	}

	// 관리자 확인용
	@GetMapping("/check-user")
	public String check(){
		return "success";
	}

	// 채팅방 생성
	@PostMapping("/room")
	public ChatRoomDto createRoom(@RequestBody String nickname, @AuthenticationPrincipal UserDetails userDetails) {
		if(nickname.equals(userDetails.getUsername())){
			return chatService.createRoom(nickname);
		}else{
			return chatService.createRoom(userDetails.getUsername());
		}
	}

	// 채팅방 기록 갖고오기
	@GetMapping("/room/enter/file/{roomId}")
	public Object readFile(@PathVariable String roomId) {
		return chatService.readFile(roomId);
	}

	// 채팅방 기록 저장하기
	@PostMapping("/room/enter/file")
	public void saveFile(@RequestBody ChatMessage chatMessage, @AuthenticationPrincipal UserDetails userDetails){
		chatMessage.setAuth(userDetails.getAuthorities().toString());
		chatService.saveFile(chatMessage);
	}
	@GetMapping ("/room/enter/{roomId}")
	LastMessage lastLine(@PathVariable String roomId) {
		return chatService.lastLine(roomId);
	}

	@GetMapping("/find-nickname/{token}")
	public String findNickname(@PathVariable String token){
		return jwtTokenProvider.getNickname(token);
	}
}