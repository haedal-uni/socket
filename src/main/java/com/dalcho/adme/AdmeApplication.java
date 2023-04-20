package com.dalcho.adme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // cache 사용
public class AdmeApplication { // chat/room
	public static void main(String[] args) {
		SpringApplication.run(AdmeApplication.class, args);
	}
}