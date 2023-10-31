package com.dalcho.adme.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Chat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_id")
	private Long idx;

	@Column(nullable = false)
	private String roomId;

	@OneToOne(fetch = LAZY)
	@JsonIgnore
	@ToString.Exclude
	@JoinColumn(name = "user_id", nullable = false)
	private User user;


	@Builder
	public Chat(String roomId, User user) {
		this.roomId = roomId;
		this.user = user;
	}

	public void addUser(User user){
		this.user = user;
	}
}
