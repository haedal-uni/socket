package com.dalcho.adme.controller;

import com.dalcho.adme.dto.ChatRoomDto;
import com.dalcho.adme.service.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class ChatRoomController {
	private final ChatServiceImpl chatService;

	// 채팅 리스트 화면
	@GetMapping("/room")
	public String rooms(Model model) {
		return "chat-only";
	}

	// 모든 채팅방 목록 반환
	@GetMapping("/rooms")
	@ResponseBody
	public List<ChatRoomDto> room() {
		return chatService.findAllRoom();
	}

	// 채팅방 생성
	@PostMapping("/room")
	@ResponseBody
	public ChatRoomDto createRoom(@RequestBody String nickname){
		return chatService.createRoom(nickname);
	}

	// 채팅방 입장 화면
	@GetMapping("/room/enter/{roomId}")
	public String roomDetail(Model model, @PathVariable String roomId){
		model.addAttribute("roomId", roomId);
		return "one-detail";
	}

	// 특정 채팅방 조회
	@GetMapping("/room/{roomId}")
	@ResponseBody
	public ChatRoomDto roomInfo(@PathVariable String roomId) {
		return chatService.findById(roomId);
	}

	// 본인 채팅방
	@GetMapping("/room/one/{nickname}")
	@ResponseBody
	public ChatRoomDto roomOne(@PathVariable String nickname) {
		return chatService.roomOne(nickname);
	}
}
