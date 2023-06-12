package com.dalcho.adme.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@ToString
public class ChatRoomMap {
	private String nickname;
	private static ChatRoomMap chatRoomMap = new ChatRoomMap();
	private Map<String, ChatRoomDto> chatRooms = new LinkedHashMap<>();
//    @PostConstruct
//    private void init() {
//        chatRooms = new LinkedHashMap<>();
//    }

	public ChatRoomMap() {
	}

	public ChatRoomMap(String nickname) {
		this.nickname = nickname;
	}
	public String getNickname() {
		return nickname;
	}

	public static ChatRoomMap getInstance() {
		return chatRoomMap;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ChatRoomMap)) return false;
		ChatRoomMap other = (ChatRoomMap) o;
		return Objects.equals(nickname, other.nickname);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nickname);
	}
}