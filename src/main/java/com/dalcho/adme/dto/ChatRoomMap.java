package com.dalcho.adme.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

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
}