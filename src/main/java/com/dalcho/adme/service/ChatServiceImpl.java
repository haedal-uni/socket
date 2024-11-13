package com.dalcho.adme.service;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.ChatMessage.MessageType;
import com.dalcho.adme.dto.ChatRoomDto;
import com.dalcho.adme.dto.ChatRoomMap;
import com.dalcho.adme.dto.LastMessage;
import com.dalcho.adme.exception.notfound.FileNotFoundException;
import com.dalcho.adme.exception.notfound.UserNotFoundException;
import com.dalcho.adme.model.Chat;
import com.dalcho.adme.model.User;
import com.dalcho.adme.model.UserRole;
import com.dalcho.adme.repository.ChatRepository;
import com.dalcho.adme.repository.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatServiceImpl {
    private final ChatRepository chatRepository;
    private final RedisService redisService;
    private Map<String, Integer> connectUsers;
    private Map<String, Integer> adminChat;
    private Map<String, Integer> userChat;

    @Value("${spring.servlet.multipart.location}")
    private String chatUploadLocation;
    private final UserRepository userRepository;
    private final Object lock = new Object();

    private Map<String, LastMessage> lastMessageMap;

    @PostConstruct // @PostConstruct는 의존성 주입이 이루어진 후 초기화를 수행하는 메서드
    private void setUp() { // 안그러면 NullPointerException
        this.connectUsers = new ConcurrentHashMap<>();
        this.adminChat = new ConcurrentHashMap<>();
        this.userChat = new ConcurrentHashMap<>();
        this.lastMessageMap = new ConcurrentHashMap<>();
    }

    public void countUser(String status, String roomId, ChatMessage chatMessage) {
        int num = connectUsers.getOrDefault(roomId, 0);
        log.info("[ countUser ] roomId : " + roomId);
        if (Objects.equals(status, "Connect")) {
            if (num < 2) { // 최대값이 2이므로 2보다 작을 때만 증가
                connectUsers.put(roomId, num + 1);
            }
            saveFile(chatMessage);
        } else if (Objects.equals(status, "Disconnect")) {
            log.info("[ DisconnectUser ] roomId : " + roomId);
            if (num > 0) {
                connectUsers.put(roomId, (num - 1));
            }
        }
        log.info("현재 인원 : " + connectUsers.get(roomId));
    }

    //채팅방 불러오기
    public List<ChatRoomDto> findAllRoom() {
        List<ChatRoomDto> chatRoomDtos = new ArrayList<>();
        List<Chat> all = chatRepository.findAll();
        try {
            for (Chat chat : all) {
                User user = userRepository.findById(chat.getUser().getId()).orElseThrow(UserNotFoundException::new);
                LastMessage lastMessage = lastLine(chat.getRoomId());
                if (!lastMessage.getMessage().isEmpty()) {
                    chatRoomDtos.add(ChatRoomDto.of(user, lastLine(chat.getRoomId())));
                }
            }
        } catch (NullPointerException e) {
            log.info(" [현재 채팅방 db 없음!] " + e);
        }
        return chatRoomDtos;
    }

    //채팅방 생성
    public ChatRoomDto createRoom(String nickname) {
        User user = userRepository.findByNickname(nickname).orElseThrow(UserNotFoundException::new);
        ChatRoomDto chatRoom = new ChatRoomDto();
        if (!chatRepository.existsByUserId(user.getId())) {
            log.info("[createRoom] roomId 값이 없음");
            chatRoom = ChatRoomDto.create();
            ChatRoomMap.getInstance().getChatRooms().put(chatRoom.getRoomId(), chatRoom);
            Chat chat = new Chat(chatRoom.getRoomId(), user);
            chatRepository.save(chat);
            chatRoom.setNickname(nickname);
            redisService.addCreateRoom("createRoom::" + nickname, chatRoom);
            return chatRoom;
        } else {
            ChatRoomDto cachedChatRoomDto = redisService.getCreateRoom("createRoom::" + nickname);
            if (cachedChatRoomDto != null) {
                log.info("[createRoom] cache 적용 o");
                cachedChatRoomDto.setLastMessage(getLastMessage(cachedChatRoomDto.getRoomId()));
                return cachedChatRoomDto;
            }else{
                log.info("[createRoom] cache 적용 x");
                Optional<Chat> findChat = chatRepository.findByUserId(user.getId());
                if (findChat.isPresent()) {
                    chatRoom.setRoomId(findChat.get().getRoomId());
                }
                LastMessage lastLine = getLastMessage(chatRoom.getRoomId());
                chatRoom.setNickname(nickname);
                redisService.addCreateRoom("createRoom::"+nickname, chatRoom);
                return ChatRoomDto.of(user, lastLine);
            }
        }
    }

    private LastMessage getLastMessage(String roomId) {
        LastMessage lastLine = lastLine(roomId);
        if (lastLine == null) {
            lastLine = LastMessage.builder()
                    .roomId(roomId)
                    .adminChat(0)
                    .userChat(0)
                    .message("환영합니다.")
                    .day("")
                    .time("")
                    .build();
        }
        return lastLine;
    }

    public ChatMessage chatAlarm(String sender, String roomId, String auth) {
        log.info("[SSE] chatAlarm");
        ChatMessage chatMessage = new ChatMessage();
        if (Objects.equals(auth, "ADMIN") && connectUsers.get(roomId) == 1) {
            chatMessage.setRoomId(roomId);
            chatMessage.setSender(sender);
            chatMessage.setMessage("고객센터에 문의한 글에 답글이 달렸습니다.");
            log.info("고객센터에 문의한 글에 답글이 달렸습니다.");
        } else if (!Objects.equals(auth, "ADMIN") && connectUsers.get(roomId) == 1) {
            chatMessage.setRoomId(roomId);
            chatMessage.setSender(sender);
            chatMessage.setMessage(sender + " 님이 답을 기다리고 있습니다.");
            log.info(sender + " 님이 답을 기다리고 있습니다.");
        }
        return chatMessage;
    }

    // 파일 저장
    public void saveFile(ChatMessage chatMessage) {
        log.info(" [ save chatFile ] start ");
        if (connectUsers.get(chatMessage.getRoomId()) != 0) {
            if (chatMessage.getType() == MessageType.JOIN) {
                reset(chatMessage.getRoomId(), chatMessage.getAuth());
            } else {
                countChat(chatMessage.getRoomId(), chatMessage.getAuth());
            }
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("roomId", chatMessage.getRoomId());
        String sender = chatMessage.getSender();
        if (chatMessage.getType() == MessageType.JOIN) {
            if (UserRole.USER.name().equals(chatMessage.getAuth())) {
                log.info("[chatServiceImpl] chatUser 사용자 추가");
                statsChat(chatMessage.getSender());
            }
            jsonObject.addProperty("type", "JOINED");
            if (lastMessageMap.containsKey(chatMessage.getRoomId())) {
                chatMessage.setMessage(lastMessageMap.get(chatMessage.getRoomId()).getMessage());
            } else {
                chatMessage.setMessage("환영합니다.");
            }
        } else {
            jsonObject.addProperty("type", chatMessage.getType().toString());
        }
        Integer adminCnt = adminChat.get(chatMessage.getRoomId());
        Integer userCnt = userChat.get(chatMessage.getRoomId());
        String days = chatMessage.getDay();
        String time = chatMessage.getTime();

        jsonObject.addProperty("sender", sender);
        jsonObject.addProperty("message", chatMessage.getMessage());
        jsonObject.addProperty("adminChat", adminCnt);
        jsonObject.addProperty("userChat", userCnt);
        jsonObject.addProperty("day", days);
        jsonObject.addProperty("time", time);

        LastMessage lastMessage = LastMessage.of(chatMessage, adminCnt, userCnt, days, time);
        lastMessageMap.put(chatMessage.getRoomId(), lastMessage);

        Gson gson = new Gson();
        String json = gson.toJson(jsonObject);

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(chatUploadLocation + "/" + chatMessage.getRoomId() + ".txt", true)))) {
            if (new File(chatUploadLocation + "/" + chatMessage.getRoomId() + ".txt").length() == 0) {
                out.println(json);
                chatAlarm(sender, chatMessage.getRoomId(), chatMessage.getAuth());
            } else {
                out.println("," + json);
            }
        } catch (IOException e) {
            log.error("[error] " + e);
        } finally {
            log.info(" [ save chatFile ] end ");
        }
    }


    public void reset(String roomId, String auth) {
        if (auth.equals("ADMIN")) {
            log.info("= = = = = = admin이 읽음 = = = = = = ");
            adminChat.putIfAbsent(roomId, 0);
            userChat.putIfAbsent(roomId, 0);
            adminChat.put(roomId, 0);
        } else {
            log.info("= = = = = = user가 읽음 = = = = = = ");
            userChat.putIfAbsent(roomId, 0);
            adminChat.putIfAbsent(roomId, 0);
            userChat.put(roomId, 0);
        }
    }

    public void countChat(String roomId, String auth) {
        if (auth.equals("ADMIN")) {
            log.info("= = = = = = admin이 보냄 = = = = = = ");
            userChat.putIfAbsent(roomId, 0);
            int num = userChat.get(roomId);
            userChat.put(roomId, num + 1);
            adminChat.put(roomId, 0);
        } else {
            log.info("= = = = = = user가 보냄 = = = = = = ");
            adminChat.putIfAbsent(roomId, 0);
            int num = adminChat.get(roomId);
            adminChat.put(roomId, num + 1);
            userChat.put(roomId, 0);
        }
    }

    public Object readFile(String roomId) {
        log.info(" [readFile]  start ");
        long startTime = System.currentTimeMillis();
        String filePath = chatUploadLocation + "/" + roomId + ".txt";
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                // 파일이 존재하지 않는 경우 새로 생성
                file.createNewFile();
            } catch (IOException e) {
                log.error("[error] " + e);
                return null;
            }
        }
        try (Stream<String> stream = Files.lines(file.toPath())) {
            List<String> lines = stream.collect(Collectors.toList());
            String jsonString = "[" + String.join(",", lines) + "]";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(jsonString);
            long stopTime = System.currentTimeMillis();
            log.info("readFile : " + (stopTime - startTime) / 1000 + " 초");
            return obj;
        } catch (NoSuchFileException e) {
            throw new FileNotFoundException();
        } catch (IOException | ParseException e) {
            log.error("[error] " + e);
            return null;
        } finally {
            log.info(" [readFile]  end ");
        }
    }

    public LastMessage lastLine(String roomId) {
        if (lastMessageMap.containsKey(roomId)) {
            log.info(" = = = MAP 활용 = = = ");
            return lastMessageMap.get(roomId);
        } else {
            log.info(" = = = MAP 활용 X = = = ");
            String filePath = chatUploadLocation + "/" + roomId + ".txt";
            File file = new File(filePath);
            // 파일의 존재 여부 확인
            if (!file.exists()) {
                try {
                    // 파일이 존재하지 않는 경우 새로 생성
                    file.createNewFile();
                    //return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    //return "";
                }
            }
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                long fileLength = file.length();
                if (fileLength > 145) {
                    randomAccessFile.seek(fileLength);
                    long pointer = fileLength - 145;
                    while (pointer > 0) {
                        randomAccessFile.seek(pointer);
                        char c = (char) randomAccessFile.read();
                        if (c == '{') {
                            break;
                        }
                        pointer--;
                    }
                    randomAccessFile.seek(pointer);
                    String line = randomAccessFile.readLine();
                    if (line == null || line.trim().isEmpty()) {
                        return null;
                    }
                    JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                    int adminChat = json.get("adminChat").getAsInt();
                    int userChat = json.get("userChat").getAsInt();
                    String message = json.get("message").getAsString().trim();
                    String messages = new String(message.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    String day = json.get("day").getAsString();
                    String time = json.get("time").getAsString();
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setRoomId(roomId);
                    chatMessage.setMessage(messages);
                    return LastMessage.of(chatMessage, adminChat, userChat, day, time);
                } else {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setRoomId(roomId);
                    chatMessage.setMessage("");
                    return LastMessage.of(chatMessage, 0, 0, "", "");
                }
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private void statsChat(String nickname) {
        redisService.addChatUserCount((LocalDate.now().getDayOfMonth() + "-ChatUser"), nickname);
    }
}