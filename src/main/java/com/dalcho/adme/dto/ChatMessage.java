package com.dalcho.adme.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
	public enum MessageType {
		JOIN, TALK, LEAVE, DELETE
	}

	private String roomId; // 채팅방 아이디
	private MessageType type; // message type
	private String sender; // message 보내는 사람
	private String message; // 내용(message)
}
