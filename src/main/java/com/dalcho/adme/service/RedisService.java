package com.dalcho.adme.service;

import com.dalcho.adme.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
	private final StringRedisTemplate redisTemplate;

	public void addRoomId(ChatMessage chatMessage) {
		long expireTimeInSeconds = 24 * 60 * 60;
		long creationTimeInMillis = System.currentTimeMillis();
		long remainingTimeInSeconds = expireTimeInSeconds - ((System.currentTimeMillis() - creationTimeInMillis) / 1000);
		redisTemplate.opsForValue().set("getRoomId - " + chatMessage.getSender(), chatMessage.getRoomId(), remainingTimeInSeconds, TimeUnit.SECONDS);
	}

	public String getRoomId(String nickname) {
		return redisTemplate.opsForValue().get("getRoomId - " + nickname);
	}

	public void deleteRoomId(String nickname) {
		redisTemplate.delete("getRoomId - " + nickname);
	}

	public void addAuth(ChatMessage chatMessage){
		long expireTimeInSeconds = 24 * 60 * 60;
		long creationTimeInMillis = System.currentTimeMillis();
		long remainingTimeInSeconds = expireTimeInSeconds - ((System.currentTimeMillis() - creationTimeInMillis) / 1000);
		redisTemplate.opsForValue().set("auth - " + chatMessage.getSender(), chatMessage.getAuth(), remainingTimeInSeconds, TimeUnit.SECONDS);
	}
	public String getAuth(String nickname){
		return redisTemplate.opsForValue().get("auth - " + nickname);
	}

	public void addSession(String sessionId, String token) {
		long expireTimeInSeconds = 24 * 60 * 60;
		long creationTimeInMillis = System.currentTimeMillis();
		long remainingTimeInSeconds = expireTimeInSeconds - ((System.currentTimeMillis() - creationTimeInMillis) / 1000);
		redisTemplate.opsForValue().set("sessionId - " + sessionId, token, remainingTimeInSeconds, TimeUnit.SECONDS);
	}

	public String getSession(String sessionId) {
		return redisTemplate.opsForValue().get("sessionId - " + sessionId);
	}

	public void deleteSession(String sessionId){
		redisTemplate.delete("sessionId - " + sessionId);
	}
}


