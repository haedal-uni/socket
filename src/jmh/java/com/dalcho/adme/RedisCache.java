package com.dalcho.adme;

import com.dalcho.adme.dto.ChatRoomDto;
import com.dalcho.adme.dto.ChatRoomMap;
import com.dalcho.adme.dto.LastMessage;
import com.dalcho.adme.exception.notfound.UserNotFoundException;
import com.dalcho.adme.model.Chat;
import com.dalcho.adme.model.User;
import com.dalcho.adme.repository.ChatRepository;
import com.dalcho.adme.repository.UserRepository;
import org.openjdk.jmh.annotations.*;
import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.ApplicationContext;

import java.util.Optional;


@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
public class RedisCache {

    private ChatRepository chatRepository;
    private UserRepository userRepository;
    private String nickname;

    @Setup
    public void setUp(){
        nickname = "ll";
        ApplicationContext context = SpringApplication.run(RedisCache.class);
        chatRepository = context.getBean(ChatRepository.class);
        userRepository = context.getBean(UserRepository.class);
    }

    @Benchmark
    public ChatRoomDto useDb(){
        return onlyDb(nickname);
    }

    @Benchmark
    public ChatRoomDto useCache(){
        return useRedis(nickname);
    }

    public ChatRoomDto onlyDb(String nickname) {
        User user = userRepository.findByNickname(nickname).orElseThrow(UserNotFoundException::new);
        ChatRoomDto chatRoom = new ChatRoomDto();
        if (!chatRepository.existsByUserId(user.getId())) {
            chatRoom = ChatRoomDto.create();
            ChatRoomMap.getInstance().getChatRooms().put(chatRoom.getRoomId(), chatRoom);
            Chat chat = new Chat(chatRoom.getRoomId(), user);
            chatRepository.save(chat);
            return chatRoom;
        } else {
            Optional<Chat> findChat = chatRepository.findByUserId(user.getId());
            String roomId = findChat.get().getRoomId();
            chatRoom.setRoomId(findChat.get().getRoomId());
            LastMessage lastLine;
            lastLine = LastMessage.builder()
                    .roomId(roomId)
                    .adminChat(0)
                    .userChat(0)
                    .message("환영합니다.")
                    .day("")
                    .time("")
                    .build();
            return ChatRoomDto.of(user, lastLine);
        }
    }



    @CachePut(key = "#nickname", value = "createRoom", unless = "#result == null", cacheManager = "cacheManager")
    public ChatRoomDto useRedis(String nickname) {
        User user = userRepository.findByNickname(nickname).orElseThrow(UserNotFoundException::new);
        ChatRoomDto chatRoom = new ChatRoomDto();
        if (!chatRepository.existsByUserId(user.getId())) {
            chatRoom = ChatRoomDto.create();
            ChatRoomMap.getInstance().getChatRooms().put(chatRoom.getRoomId(), chatRoom);
            Chat chat = new Chat(chatRoom.getRoomId(), user);
            chatRepository.save(chat);
            return chatRoom;
        } else {
            Optional<Chat> findChat = chatRepository.findByUserId(user.getId());
            String roomId = findChat.get().getRoomId();
            chatRoom.setRoomId(findChat.get().getRoomId());
            LastMessage lastLine;
            lastLine = LastMessage.builder()
                    .roomId(roomId)
                    .adminChat(0)
                    .userChat(0)
                    .message("환영합니다.")
                    .day("")
                    .time("")
                    .build();
            return ChatRoomDto.of(user, lastLine);
        }
    }
}
