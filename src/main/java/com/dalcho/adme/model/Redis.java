package com.dalcho.adme.model;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Id;
import java.util.concurrent.TimeUnit;

@Getter
@RedisHash("chatRoom")
@ToString
@NoArgsConstructor
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Redis {
	@Id
	private String id;
	@Indexed // 필드 값으로 데이터 찾을 수 있게 하는 어노테이션(findByAccessToken)
	private String nickname;

	public void setEmail(String email) {
		this.email = email;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Indexed
	private String email;
	private String accessToken;
	private String roomId;
	@TimeToLive(unit = TimeUnit.HOURS)
	private Long expiration;
	@Builder
	public Redis(String nickname, String roomId, Long hour){
		this.nickname = nickname;
		this.roomId = roomId;
		this.expiration = hour;
	}
}
