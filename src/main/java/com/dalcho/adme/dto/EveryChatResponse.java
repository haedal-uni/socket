package com.dalcho.adme.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EveryChatResponse {

	public enum ResponseType {
		SUCCESS, CANCEL, TIMEOUT;
	}
	private ResponseType type;
	private String roomId;
	private String sessionId;
}
