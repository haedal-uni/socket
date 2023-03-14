package com.dalcho.adme.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestService {
	@Async("executor")
	public void asyncMethod(int i) {
		try {
			Thread.sleep(500);
			System.out.println("[AsyncMethod]"+"-"+i);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TestService testService = new TestService();
		for(int i=0; i<20; i++) {
			testService.asyncMethod(i);
		}
	}
}
