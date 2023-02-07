package com.dalcho.adme.controller;

import com.dalcho.adme.dto.ChatMessage;
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
@Slf4j
public class ChatRoomController {
	private final ChatServiceImpl chatService;

	// 채팅 리스트 화면
	@GetMapping("/room")
	public String rooms(Model model) {
		return "chat-list";
	}

	@GetMapping("/draw")
	public String draw(){
		return "draw";
	}

	// 모든 채팅방 목록 반환(관리자)
	@GetMapping("/rooms")
	@ResponseBody
	public List<ChatRoomDto> room() {
		return chatService.findAllRoom();
	}

	// 본인 채팅방(일반 유저)
	@GetMapping("/room/one/{nickname}")
	@ResponseBody
	public ChatRoomDto roomOne(@PathVariable String nickname) {
		return chatService.roomOne(nickname);
	}

	// 채팅방 생성
	@PostMapping("/room")
	@ResponseBody
	public ChatRoomDto createRoom(@RequestBody String nickname) {
		return chatService.createRoom(nickname);
	}

	// 채팅방 입장 화면
	@GetMapping("/room/enter/{roomId}")
	public String roomDetail(Model model, @PathVariable String roomId) {
		model.addAttribute("roomId", roomId);
		return "chat-room";
	}

	// 완료된 채팅방 삭제하기
	@DeleteMapping("/room/one/{roomId}")
	@ResponseBody
	public void deleteRoom(@PathVariable String roomId) {
		chatService.deleteRoom(roomId);
	}

	// 삭제 후 채팅방 재 접속 막기
	@GetMapping("/room/{roomId}")
	@ResponseBody
	public boolean getRoomInfo(@PathVariable String roomId) {
		return chatService.getRoomInfo(roomId);
	}

	// 채팅방 기록 갖고오기
	@GetMapping("/room/enter/{roomId}/{roomName}")
	@ResponseBody
	public Object readFile(@PathVariable String roomId, @PathVariable String roomName) {
		return chatService.readFile(roomId);
	}

	// 채팅방 기록 저장하기
	@PostMapping("/room/enter/{roomId}/{roomName}")
	@ResponseBody
	public void saveFile(@PathVariable String roomId, @PathVariable String roomName, @RequestBody ChatMessage chatMessage){
		chatService.saveFile(chatMessage);
	}
}
