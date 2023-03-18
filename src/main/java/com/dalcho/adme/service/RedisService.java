package com.dalcho.adme.service;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.RedisResponseDto;
import com.dalcho.adme.model.Redis;
import com.dalcho.adme.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
	private final RedisTemplate<String, String> redisTemp;
	private final StringRedisTemplate redisTemplate;
	private final RedisRepository redisRepository;

	/*
	StringRedisTemplate
	 */
	// string (opsForValue)
//	public void setRedisTemplate(ChatMessage chatMessage, Long expirationTime){
//		redisTemplate.opsForValue().set(chatMessage.getSender(), chatMessage.getRoomId(), expirationTime, TimeUnit.HOURS);
//		//키가 이미 있다면 마지막에 Set한 값으로 덮어씀
//	}
//	public String getRedisTemplate(String key){
//		return redisTemplate.opsForValue().get(key);
//	}

	public void addRedis(ChatMessage chatMessage, Long hours){
		Redis redis = new Redis(chatMessage.getSender(), chatMessage.getRoomId(), hours);
		Redis save = redisRepository.save(redis);
	}
	public String getRedis(String key){
		Redis byNickname = redisRepository.findByNickname(key);
		return RedisResponseDto.of(byNickname).getRoomId();
	}

	public void deleteRedis(String key){
		redisTemplate.delete(key);
	}


	/*
	RedisTemplate
	 */
//	public void setRedisValue(ChatMessage chatMessage, Long hours){
//		ValueOperations<String, String> values = redisTemp.opsForValue();
//		values.set(chatMessage.getSender(), chatMessage.getRoomId(), hours);
//	}
//	public String getRedisValue(String key){
//		ValueOperations<String, String> values = redisTemp.opsForValue();
//		return values.get(key);
//	}


}


