package com.dalcho.adme.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EveryChatResponse {

	public enum ResponseType {
		SUCCESS, CANCEL, TIMEOUT;
	}
	private ResponseType type;
	private String roomId;
	private String nickname;
}
