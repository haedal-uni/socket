package com.dalcho.adme.dto;

import com.dalcho.adme.model.User;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChatRoomDto implements Serializable { // 일반 crud에서 쓰임
	private String roomId;
	private String nickname;
	private LastMessage lastMessage;// Composition(has-a 관계)

	public ChatRoomDto() {
	}

	public static ChatRoomDto create() {
		ChatRoomDto room = new ChatRoomDto();
		room.roomId = UUID.randomUUID().toString();
		return room;
	}

	public static ChatRoomDto of(User user, LastMessage lastMessage) {
		return ChatRoomDto.builder()
				.nickname(user.getNickname())
				.lastMessage(lastMessage)
				.build();
	}
}
