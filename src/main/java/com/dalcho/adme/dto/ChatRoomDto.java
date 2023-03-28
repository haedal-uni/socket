package com.dalcho.adme.dto;

import com.dalcho.adme.model.Chat;
import com.dalcho.adme.model.User;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class ChatRoomDto { // 일반 crud에서 쓰임
	private String roomId; // 채팅방 아이디
	private String roomName; // 채팅방 이름(사용자가 설정한 이름)
	private String nickname;
	private Integer adminChat;
	private Integer userChat;

	public ChatRoomDto() {
	}

	public static ChatRoomDto create(String name) {
		ChatRoomDto room = new ChatRoomDto();
		room.roomId = UUID.randomUUID().toString();
		room.roomName = name;
		return room;
	}

	public static ChatRoomDto of(Chat chat, User user, List<Integer> list) {
		return ChatRoomDto.builder().
				roomId(chat.getRoomId())
				.nickname(user.getNickname())
				.adminChat(list.get(0))
				.userChat(list.get(1))
				.build();
	}
}
