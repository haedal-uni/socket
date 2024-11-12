package com.dalcho.adme.service;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.ChatRoomDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
	private final RedisTemplate<String, String> redisTemplate;
	private final RedisTemplate<String, ChatRoomDto> chatRoomRedisTemplate;
	private static final long expirationTimeInSeconds = 24*60*60;
	private static final long STATSTIME = 26;

	public void addCreateRoom(String key, ChatRoomDto chatRoomDto){
		chatRoomRedisTemplate.opsForValue().set(key, chatRoomDto);
		redisTemplate.expire(key, STATSTIME, TimeUnit.HOURS);
	}

	public ChatRoomDto getCreateRoom(String key){
		return chatRoomRedisTemplate.opsForValue().get(key);
	}

	public void addLoginUserCount(String key, String nickname){
		redisTemplate.opsForSet().add(key, nickname);
		redisTemplate.expire(key, STATSTIME, TimeUnit.HOURS);
	}

	public void deleteLoginUserCount(String key){
		redisTemplate.delete(key);
	}

	public void addChatUserCount(String key, String nickname){
		redisTemplate.opsForSet().add(key, nickname);
		redisTemplate.expire(key, STATSTIME, TimeUnit.HOURS);
	}

	public void deleteChatUserCount(String key){
		redisTemplate.delete(key);
	}

	public void addRoomId(ChatMessage chatMessage) {
		long creationTimeInMillis = System.currentTimeMillis();
		long remainingTimeInSeconds = expirationTimeInSeconds - ((System.currentTimeMillis() - creationTimeInMillis) / 1000);
		redisTemplate.opsForValue().set("getRoomId - " + chatMessage.getSender(), chatMessage.getRoomId(), remainingTimeInSeconds, TimeUnit.SECONDS);
	}

	public String getRoomId(String nickname) {
		return redisTemplate.opsForValue().get("getRoomId - " + nickname);
	}

	public void deleteRoomId(String nickname) {
		redisTemplate.delete("getRoomId - " + nickname);
	}

	public void addAuth(ChatMessage chatMessage){
		long creationTimeInMillis = System.currentTimeMillis();
		long remainingTimeInSeconds = expirationTimeInSeconds - ((System.currentTimeMillis() - creationTimeInMillis) / 1000);
		redisTemplate.opsForValue().set("auth - " + chatMessage.getSender(), chatMessage.getAuth(), remainingTimeInSeconds, TimeUnit.SECONDS);
	}

	public String getAuth(String nickname){
		return redisTemplate.opsForValue().get("auth - " + nickname);
	}

	public void addSession(String sessionId, String token) {
		long creationTimeInMillis = System.currentTimeMillis();
		long remainingTimeInSeconds = expirationTimeInSeconds - ((System.currentTimeMillis() - creationTimeInMillis) / 1000);
		redisTemplate.opsForValue().set("sessionId - " + sessionId, token, remainingTimeInSeconds, TimeUnit.SECONDS);
	}

	public String getSession(String sessionId) {
		return redisTemplate.opsForValue().get("sessionId - " + sessionId);
	}

	public void deleteSession(String sessionId){
		redisTemplate.delete("sessionId - " + sessionId);
	}
}


