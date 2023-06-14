package com.dalcho.adme.config;

import com.dalcho.adme.dto.ChatRoomDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {
	@Value("${spring.redis.host}")
	private String redisHost;
	@Value("${spring.redis.port}")
	private int redisPort;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost, redisPort);
	}

	@Bean //Redis 데이터에 쉽게 접근하기 위한 코드
	public RedisTemplate<?, ?> redisTemplate() { //RedisTemplate 에 LettuceConnectionFactory 을 적용
		RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());//redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Object.class));
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	@Bean
	public RedisTemplate<String, ChatRoomDto> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, ChatRoomDto> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatRoomDto.class));
		return redisTemplate;
	}
//	@Bean
//	public CacheManager cacheManager() {
//		RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory());
//		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
//				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))// Value Serializer 변경
//				.entryTtl(Duration.ofMinutes(30));
//		builder.cacheDefaults(configuration);
//		return builder.build();
//	}
	@Bean
	public CacheManager cacheManager1() { // TTL
		long expireTimeInSeconds = 24 * 60 * 60;
		long creationTimeInMillis = System.currentTimeMillis();
		long remainingTimeInSeconds = expireTimeInSeconds - ((System.currentTimeMillis() - creationTimeInMillis) / 1000);
		RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofSeconds(remainingTimeInSeconds))
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
		RedisCacheConfiguration cacheConfigWithoutNullValues = RedisCacheConfiguration.defaultCacheConfig()
				.disableCachingNullValues()
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
		return RedisCacheManager.builder(redisConnectionFactory())
				.cacheDefaults(cacheConfigWithoutNullValues)
				.withCacheConfiguration("roomId", cacheConfig)
				.withCacheConfiguration("createRoom", cacheConfig)
				.build();
	}
}
