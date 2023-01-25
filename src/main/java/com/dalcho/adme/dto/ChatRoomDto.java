package com.dalcho.adme.dto;

import com.dalcho.adme.model.Socket;
import lombok.*;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class ChatRoomDto {
	private String roomId; // 채팅방 아이디
	private String roomName; // 채팅방 이름(사용자가 설정한 이름)
	private String nickname;
	//private int userCount; // 채팅방 인원수
	//private int maxUserCnt; // 채팅방 최대 인원 제한
	//private Set<WebSocketSession> sessions = new HashSet<>();

	public ChatRoomDto() {

	}
	//WebSocketSession은 Spring에서 Websocket Connection이 맺어진 세션

	public static ChatRoomDto create(String name) {
		ChatRoomDto room = new ChatRoomDto();
		room.roomId = UUID.randomUUID().toString();
		room.roomName = name;
		return room;
	}


	public static ChatRoomDto of (Socket socket){
		return ChatRoomDto.builder()
				.roomId(socket.getRoomId())
				.nickname(socket.getNickname())
				.build();
	}
}
