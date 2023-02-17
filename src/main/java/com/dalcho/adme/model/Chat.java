package com.dalcho.adme.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@ToString
public class Chat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_id")
	private Long idx;
	@Column(nullable = false)
	private String nickname;
	@Column(nullable = false)
	private String roomId;

	@Builder
	public Chat(String roomId, String nickname) {
		this.roomId = roomId;
		this.nickname = nickname;
	}
}
