package com.dalcho.adme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
	public enum MessageType {
		ENTER, TALK, QUIT
	}

	private MessageType type; // message type

	private String roomId; //채팅방 ID(방 번호)

	private String sender; // message 보내는 사람

	private String message; // 내용(message)

	private int userCount; // 채팅방 인원수
}
