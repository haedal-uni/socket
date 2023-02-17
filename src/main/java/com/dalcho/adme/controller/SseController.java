package com.dalcho.adme.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
public class SseController {
	private static final Map<String, SseEmitter> CLIENTS = new ConcurrentHashMap<>();
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
	@GetMapping("/api/subscribe")
	public SseEmitter subscribe(String id) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		CLIENTS.put(id, emitter);
		emitter.onTimeout(() -> {
			log.info("onTimeout callback");
			//CLIENTS.remove(id);
			emitter.complete();
		});
		emitter.onCompletion(() -> CLIENTS.remove(id));
		return emitter;
	}

	@GetMapping("/api/publish")
	public void publish(String message) {
		Set<String> deadIds = new HashSet<>();
		CLIENTS.forEach((id, emitter) -> {
			try {
				emitter.send(message, MediaType.APPLICATION_JSON);
			} catch (Exception e) {
				e.getStackTrace();
				deadIds.add(id);
				log.warn("disconnected id : {}", id);
			}
		});

		deadIds.forEach(CLIENTS::remove);
	}
}
