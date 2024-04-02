package com.dalcho.adme.controller;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.service.ChatServiceImpl;
import com.dalcho.adme.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
@Slf4j
public class SseController {
    private final ChatServiceImpl chatService;
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private static final Map<String, SseEmitter> CLIENTS = new ConcurrentHashMap<>();

    public final RedisService redisService;

    @GetMapping("/alarm/subscribe/{id}")
    public SseEmitter subscribe(@PathVariable String id) throws IOException {
        log.info("[SSE] SUBSCRIBE");
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitter.onTimeout(() -> CLIENTS.remove(id));
        emitter.onCompletion(() -> CLIENTS.remove(id));
        CLIENTS.put(id, emitter);
        emitter.send(SseEmitter.event().name("connect") // 해당 이벤트의 이름 지정
                .data("connected!")); // 503 에러 방지를 위한 더미 데이터
        return emitter;
    }

    @GetMapping( "/alarm/publish")
    @Async // 비동기
    public void publish(@RequestParam String sender, @RequestParam String roomId, @AuthenticationPrincipal UserDetails userDetails) {
        String auth;
        if(userDetails==null){
            auth = redisService.getAuth(sender);
        }else{
            auth = userDetails.getAuthorities().toString();
        }
        Set<String> deadIds = new HashSet<>();
        CLIENTS.forEach((id, emitter) -> {
            try {
                ChatMessage chatMessage = chatService.chatAlarm(sender, roomId, auth);
                emitter.send(chatMessage, MediaType.APPLICATION_JSON);
                log.info("[SSE] send 완료");
            } catch (Exception e) {
                log.error("[error]  " + e);
                // Error handling
                deadIds.add(id);
                log.warn("disconnected id : {}", id);
            }
            deadIds.forEach(CLIENTS::remove);
        });
    }


    private void removeClient(String id) {
        CLIENTS.remove(id);
        // Additional cleanup, if necessary
    }

//    private ChatMessage getChatMessage(String sender, String roomId) {
//        // Create and return a ChatMessage object
//        return new ChatMessage(sender, roomId);
//    }
}
