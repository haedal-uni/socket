package com.dalcho.adme.config.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
//@RequiredArgsConstructor
public class ChatMessageConsumer{

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void onMessage(Message message) { // Queue에서 message를 구독
        try {
            byte[] body = message.getBody();
            String receivedMessage = new String(body);
            System.out.println("Received message: " + receivedMessage);

            log.info("Received message: " + new String(message.getBody()));
        } catch (Exception e) {
            log.error("Error processing message: " + e.getMessage());
        }
    }
}