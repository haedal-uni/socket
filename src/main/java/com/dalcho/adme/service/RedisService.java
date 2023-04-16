package com.dalcho.adme.service;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.RedisResponseDto;
import com.dalcho.adme.model.Redis;
import com.dalcho.adme.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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

	@Cacheable(key = "#chatMessage.sender", unless = "#chatMessage.sender == 'null'", value = "chatMessage.roomId") //,
	public void addRedis(ChatMessage chatMessage, Long hours){
		Redis redis = new Redis(chatMessage.getSender(), chatMessage.getRoomId(), hours);
		Redis save = redisRepository.save(redis);
	}

	@Cacheable(key = "#nickname", value = "chatMessage.value")
	public String getRedis(String nickname){
		Redis byNickname = redisRepository.findByNickname(nickname);
		return RedisResponseDto.of(byNickname).getRoomId();
	}

	public void addToken(String email, String accessToken){
		Redis redis = new Redis();
		redis.setEmail(email);
		redis.setAccessToken(accessToken);
		Redis save = redisRepository.save(redis);
		System.out.println("accessToken save : " + save);
	}

	public String getToken(String email){
		Redis byEmail = redisRepository.findByEmail(email);
		return byEmail.getAccessToken();
	}

	public void deleteRedis(String nickname){
		Redis byNickname = redisRepository.findByNickname(nickname);
		redisRepository.delete(byNickname);
	}
	public void deleteToken(String email){
		Redis byEmail = redisRepository.findByEmail(email);
		redisRepository.delete(byEmail);
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


