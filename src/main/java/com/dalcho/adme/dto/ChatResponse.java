package com.dalcho.adme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
	public ChatResponse(ResponseType success, String sessionId) {
	}

	public enum ResponseType {
		SUCCESS, CANCEL, TIMEOUT;
	}
	private ResponseType type;
	private String roomId;
	private String sessionId;
}
