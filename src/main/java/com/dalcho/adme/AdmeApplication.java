package com.dalcho.adme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableCaching // cache 사용
public class AdmeApplication { // chat/room
	public static void main(String[] args) {
		SpringApplication.run(AdmeApplication.class, args);
	}
}