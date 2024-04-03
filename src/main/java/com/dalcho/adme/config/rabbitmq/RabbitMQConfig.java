package com.dalcho.adme.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
//@EnableRabbit
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.connect.queue}")
    private String connectQueue;

    @Value("${rabbitmq.send.queue}")
    private String sendQueue;

    @Value("${rabbitmq.disconnect.queue}")
    private String disconnectQueue;

    @Value("${rabbitmq.connect.exchange}")
    private String connectExchange;

    @Value("${rabbitmq.send.exchange}")
    private String sendExchange;

    @Value("${rabbitmq.disconnect.exchange}")
    private String disconnectExchange;

//    @Value("${rabbitmq.exchange.name}")
//    private String exchange;
//
//    @Value("${rabbitmq.queue.name}")
//    private String queue;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;


//    @Bean
//    TopicExchange exchange() {
//        return new TopicExchange(exchange);
//    }
//    @Bean
//    public Queue helloqueue() {
//        return new Queue(queue);
//    }
    /**
     * 지정된 큐 이름으로 Queue 빈을 생성
     *
     * @return Queue 빈 객체
     */

    @Bean
    public Queue connectQueue() {
        return new Queue(connectQueue, true);
    }

    @Bean
    public Queue sendQueue() {
        return new Queue(sendQueue, true);
    }

    @Bean
    public Queue disconnectQueue() {
        return new Queue(disconnectQueue, true);
    }

    @Bean
    public TopicExchange connectExchange() {
        return new TopicExchange(connectExchange);
    }

    @Bean
    public TopicExchange sendExchange() {
        return new TopicExchange(sendExchange);
    }

    @Bean
    public TopicExchange disconnectExchange() {
        return new TopicExchange(disconnectExchange);
    }

    /*
     * Exchange와 Queue를 바인딩하기 위한 Binding 빈 생성
     * @param queue Queue 빈 객체
     * @param exchange DirectExchange 빈 객체
     * @return Binding 빈 객체
     */

//    @Bean
//    public Binding binding() {
//        // Consumer의 고유한 번호를 기반으로 라우팅 키를 생성하여 사용
//        return BindingBuilder
//                .bind(helloqueue())
//                .to(exchange())
//                .with(routingKey);
//    }
    @Bean
    public Binding connectBinding() {
        return BindingBuilder
                .bind(connectQueue())
                .to(connectExchange())
                .with(routingKey);
    }

    @Bean
    public Binding sendBinding() {
        return BindingBuilder
                .bind(sendQueue())
                .to(sendExchange())
                .with(routingKey);
    }

    @Bean
    public Binding disconnectBinding() {
        return BindingBuilder
                .bind(disconnectQueue())
                .to(disconnectExchange())
                .with(routingKey);
    }


    /**
     * RabbitMQ와의 연결을 관리하는 클래스
     *
     * @return ConnectionFactory 빈 객체
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host);
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    /*
     * RabbitMQ와의 메시지 통신을 담당하는 클래스
     * @param connectionFactory ConnectionFactory 빈 객체
     * @return RabbitTemplate 빈 객체
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        //rabbitTemplate.setExchange(exchange);
        rabbitTemplate.setRoutingKey(routingKey);
        return rabbitTemplate;
    }

    /**
     * Jackson library를 사용해서 msg를 JSON 형식으로 변환하는 BEAN 생성
     * @return MessageConverter 빈 객체
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        // RabbitMQ 메시지 리스너 컨테이너 설정
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //container.setQueueNames(queueName);
        return container;
    }
}