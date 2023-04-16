package com.dalcho.adme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
//@EnableRedisHttpSession //Redis에 세션 데이터를 저장
@EnableCaching // cache 사용
public class AdmeApplication { // chat/room
	public static void main(String[] args) {
		SpringApplication.run(AdmeApplication.class, args);
	}
}