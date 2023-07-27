package com.dalcho.adme.config;

import com.dalcho.adme.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener { // 구독자
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;


    @Override // Redis 메시지를 수신하면 호출되는 메소드
    public void onMessage(Message message, byte[] pattern) {
        try{
            System.out.println(" on message ");
            // Redis로부터 수신된 메시지 처리 로직을 구현
            String channel = new String(message.getChannel());
            System.out.println(" [ onMessage ] channel : " + channel);
            String msg = redisTemplate.getStringSerializer().deserialize(message.getBody());

            String data = objectMapper.readValue(msg, String.class);
            System.out.println(data);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

