package com.dalcho.adme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration // Bean 등록
@EnableAsync // Async 설정
public class AsyncConfiguration {
	@Bean(name="executor")
	public Executor asyncThreadPool() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(3); // 생성할 개수(thread pool에 항상 존재하는 최소 개수)
		taskExecutor.setMaxPoolSize(10); // 동시 동작하는 최대 Thread의 수
		taskExecutor.setQueueCapacity(15); // 큐의 사이즈
		taskExecutor.setThreadNamePrefix("Async-Executor-");
		taskExecutor.setDaemon(true);
		taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		taskExecutor.setAwaitTerminationSeconds(60);
		taskExecutor.initialize();
		return taskExecutor;
	}
}
/*
*@TransactionalEventListener
 */