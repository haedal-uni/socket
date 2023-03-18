package com.dalcho.adme.dto;

import com.dalcho.adme.model.Redis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RedisResponseDto {
	private String nickname;
	private String roomId;
	private Long hour;

	public static RedisResponseDto of(Redis redis){
		return RedisResponseDto.builder()
				.nickname(redis.getNickname())
				.roomId(redis.getRoomId())
				.hour(redis.getExpiration())
				.build();
	}
}
