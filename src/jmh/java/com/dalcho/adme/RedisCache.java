package com.dalcho.adme;

import com.dalcho.adme.dto.ChatRoomDto;
import com.dalcho.adme.exception.notfound.UserNotFoundException;
import com.dalcho.adme.model.Chat;
import com.dalcho.adme.model.User;
import com.dalcho.adme.repository.ChatRepository;
import com.dalcho.adme.repository.UserRepository;
import com.dalcho.adme.service.RedisService;
import org.openjdk.jmh.annotations.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;

/**
 * "DB 조회" vs "Redis 캐시 조회"를 공정하게 비교한다.
 *
 * 기존 코드의 문제: @CachePut을 벤치마크 객체(Spring 빈이 아님)에서 this로 직접 호출 → AOP 프록시를
 * 안 거치므로 캐시 애노테이션이 완전히 무시됐다. 즉 useDb/useCache가 둘 다 DB만 조회하는 동일 코드였다.
 *
 * 수정: 실제 RedisService 빈을 통해 Redis를 왕복시키고, setup에서 캐시를 미리 채워
 * "캐시 히트 경로"를 측정한다. 두 경로 모두 read-only라 반복 실행에도 상태가 변하지 않는다.
 */
@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
public class RedisCache {

    private ConfigurableApplicationContext context;
    private ChatRepository chatRepository;
    private UserRepository userRepository;
    private RedisService redisService;
    private String nickname;
    private String cacheKey;

    @Setup
    public void setUp() {
        nickname = "abc.1234";
        cacheKey = "createRoom::" + nickname;

        context = SpringApplication.run(AdmeApplication.class);
        chatRepository = context.getBean(ChatRepository.class);
        userRepository = context.getBean(UserRepository.class);
        redisService = context.getBean(RedisService.class);

        // 캐시 히트 경로를 측정하려면 Redis에 값이 미리 존재해야 한다.
        User user = userRepository.findByNickname(nickname).orElseThrow(UserNotFoundException::new);
        ChatRoomDto cached = new ChatRoomDto();
        chatRepository.findByUserId(user.getId()).ifPresent(chat -> cached.setRoomId(chat.getRoomId()));
        cached.setNickname(nickname);
        redisService.addCreateRoom(cacheKey, cached);
    }

    @TearDown
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Benchmark
    public ChatRoomDto useDb() {
        return onlyDb(nickname);
    }

    @Benchmark
    public ChatRoomDto useCache() {
        return useRedis(nickname);
    }

    /** DB에서 방 정보를 읽어온다 (캐시 미스 시 실제 경로). */
    public ChatRoomDto onlyDb(String nickname) {
        User user = userRepository.findByNickname(nickname).orElseThrow(UserNotFoundException::new);
        Optional<Chat> findChat = chatRepository.findByUserId(user.getId());
        ChatRoomDto chatRoom = new ChatRoomDto();
        findChat.ifPresent(chat -> chatRoom.setRoomId(chat.getRoomId()));
        chatRoom.setNickname(nickname);
        return chatRoom;
    }

    /** Redis 캐시에서 방 정보를 읽어온다 (캐시 히트 경로). */
    public ChatRoomDto useRedis(String nickname) {
        return redisService.getCreateRoom("createRoom::" + nickname);
    }
}
