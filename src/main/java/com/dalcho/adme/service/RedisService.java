package com.dalcho.adme.service;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.model.Redis;
import com.dalcho.adme.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
	private final RedisTemplate<String, String> redisTemp;
	private final StringRedisTemplate redisTemplate;
	private final RedisRepository redisRepository;
	// expireTimeInSeconds: key의 만료 시간을 초 단위로 설정
// remainingTimeInSeconds: key의 만료 시간까지 남은 시간 (하루를 기준으로 계산)


	/*
	StringRedisTemplate
	 */
	// string (opsForValue)
	//키가 이미 있다면 마지막에 Set한 값으로 덮어씀
	@Cacheable(key = "'roomId:' + #chatMessage.roomId", value = "roomId", unless = "#chatMessage.roomId == null")
	public void addRedis(ChatMessage chatMessage){
		long expireTimeInSeconds = 24 * 60 * 60;
		long creationTimeInMillis = System.currentTimeMillis();
		long remainingTimeInSeconds = expireTimeInSeconds - ((System.currentTimeMillis() - creationTimeInMillis) / 1000);
		redisTemplate.opsForValue().set(chatMessage.getSender(), chatMessage.getRoomId(), remainingTimeInSeconds, TimeUnit.SECONDS);
	}

	@Cacheable(value = "roomId", key = "#nickname")
	public String getRedis(String nickname){
		return redisTemplate.opsForValue().get(nickname);
	}

/* Repository
	@Cacheable(key = "#chatMessage.roomId", value = "roomId", unless = "#chatMessage.roomId == 'null'")
	public void addRedis(ChatMessage chatMessage, Long hours){
		Redis redis = new Redis(chatMessage.getSender(), chatMessage.getRoomId(), hours);
		Redis save = redisRepository.save(redis);
	}

	@Cacheable(value = "roomId")
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
*/
	@Cacheable(key = "#email", value = "accessToken", unless = "#result == null || #accessToken == null")
	public void addToken(String email, String accessToken){
		long expireTimeInSeconds = 24 * 60 * 60;
		long creationTimeInMillis = System.currentTimeMillis();
		long remainingTimeInSeconds = expireTimeInSeconds - ((System.currentTimeMillis() - creationTimeInMillis) / 1000);
		redisTemplate.opsForValue().set(email, accessToken, remainingTimeInSeconds, TimeUnit.SECONDS);
		//키가 이미 있다면 마지막에 Set한 값으로 덮어씀
	}
	@Cacheable(value = "accessToken")
	public String getToken(String email){
		return redisTemplate.opsForValue().get(email);
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


