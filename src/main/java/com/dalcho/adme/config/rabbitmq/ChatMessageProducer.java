//package com.dalcho.adme.config.rabbitmq;
//
//import com.dalcho.adme.dto.ChatMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
////@Component
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ChatMessageProducer {
//
//    private final RabbitTemplate rabbitTemplate;
//
//    @Value("${rabbitmq.exchange.name}")
//    private String exchange;
//
//    public void sendMessage(ChatMessage message, String roomId) {
//        log.info("message send : {}", message);
//        rabbitTemplate.convertAndSend(exchange, roomId, message);
//    }
//}