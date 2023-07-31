package com.dalcho.adme.config;

import com.dalcho.adme.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher { // 발행자(Publisher) 추가

    private final RedisTemplate<String, Object> redisTemplate;

//    public void publish(ChannelTopic topic, String message) {
//        System.out.println(" [publish] topic.getTopic() : " + topic.getTopic());
//        System.out.println("message : " + message);
//        redisTemplate.convertAndSend(topic.getTopic(), message);
//    }
    public void publish(ChannelTopic topic, ChatMessage message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
