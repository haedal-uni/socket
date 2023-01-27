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

	public ChatRoomDto() {
	}


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
