package com.dalcho.adme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfiguration { // Async 설정
	@Bean
	public Executor asyncThreadPool() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

		taskExecutor.setCorePoolSize(3);
		taskExecutor.setMaxPoolSize(30);
		taskExecutor.setQueueCapacity(10);
		taskExecutor.setThreadNamePrefix("Async-Executor-");
		taskExecutor.setDaemon(true);
		taskExecutor.initialize();

		return taskExecutor;
	}
}
/*
기본적으로 Spring Event 는 동기적이다. 하지만 @Async 를 통해 비동기로 동작할 수 있다.
Executor 쓰레드풀을 사용

*@TransactionalEventListener
 */