package com.dalcho.adme.dto;

import com.dalcho.adme.model.User;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class ChatRoomDto implements Serializable { // 일반 crud에서 쓰임
	private String roomId; // 채팅방 아이디
	private String roomName; // 채팅방 이름(사용자가 설정한 이름)
	private String nickname;
	private Integer adminChat;
	private Integer userChat;
	private String message;
	private String day;
	private String time;

	public ChatRoomDto() {
	}

	public static ChatRoomDto create() {
		ChatRoomDto room = new ChatRoomDto();
		room.roomId = UUID.randomUUID().toString();
		return room;
	}

	public static ChatRoomDto of(String roomId, User user, LastMessage lastMessage) {
		return ChatRoomDto.builder()
				.roomId(roomId)
				.nickname(user.getNickname())
				.adminChat(lastMessage.getAdminChat())
				.userChat(lastMessage.getUserChat())
				.message(lastMessage.getMessage())
				.day(lastMessage.getDay())
				.time(lastMessage.getTime())
				.build();
	}
}
