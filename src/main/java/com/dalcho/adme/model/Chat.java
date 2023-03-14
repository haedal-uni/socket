package com.dalcho.adme.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

	@OneToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	@ToString.Exclude
	@JoinColumn(name = "user_id", nullable = false)
	private User users;


	@Builder
	public Chat(String roomId, User user) {
		this.roomId = roomId;
		this.users = user;
	}

	public void addUser(User user){
		user.addChat(this);
		this.users = user;
	}
}
