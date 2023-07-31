package com.dalcho.adme.config;

import com.dalcho.adme.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber implements MessageListener { // 구독자
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    @Override // Redis 메시지를 수신하면 호출되는 메소드
    public void onMessage(Message message, byte[] pattern) {
        try{
            // Redis로부터 수신된 메시지 처리 로직을 구현
            String channel = new String(message.getChannel());
            log.info("Received message from channel: " + channel);

            // publish에서 String으로 처리 할 경우
            String msg = new String(message.getBody(), StandardCharsets.UTF_8); // Convert message body to String

            // publish에서 ChatMessage로 처리 할 경우
            ChatMessage chatMessage = objectMapper.readValue(msg, ChatMessage.class);
            log.info("chatMessage : " + chatMessage);

            // //String data = objectMapper.readValue(msg, String.class);
            //JsonNode jsonNode = objectMapper.readTree(msg); // JSON 문자열을 파싱

            try (Jedis jedis = new Jedis("localhost")) {
                // 특정 채널의 구독자 수 조회
                Map<String, String> stringStringMap = jedis.pubsubNumSub(channel);
                log.info("구독자 수: " + stringStringMap);
            }

            //messagingTemplate.convertAndSend(channel, jsonNode);
            messagingTemplate.convertAndSend(channel, chatMessage);


        } catch (Exception e){
            log.error(e.getMessage());
        }
    }
}

