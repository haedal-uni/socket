package com.dalcho.adme.service;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.ChatRoomDto;
import com.dalcho.adme.dto.ChatRoomMap;
import com.dalcho.adme.exception.CustomException;
import com.dalcho.adme.exception.notfound.ChatRoomNotFoundException;
import com.dalcho.adme.exception.notfound.FileNotFoundException;
import com.dalcho.adme.exception.notfound.UserNotFoundException;
import com.dalcho.adme.model.Chat;
import com.dalcho.adme.model.User;
import com.dalcho.adme.repository.ChatRepository;
import com.dalcho.adme.repository.UserRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatServiceImpl {
	private final ChatRepository chatRepository;
	private Map<String, Integer> connectUsers;
	@Value("${spring.servlet.multipart.location}")
	private String chatUploadLocation;
	private final UserRepository userRepository;

	@PostConstruct // @PostConstruct는 의존성 주입이 이루어진 후 초기화를 수행하는 메서드
	private void setUp() { // 안그러면 NullPointerException
		this.connectUsers = new HashMap<>();
	}

	public void connectUser(String status, String roomId){
		if (Objects.equals(status, "Connect")){
			connectUsers.putIfAbsent(roomId, 0); // 값이 없으면 이걸 수행하고 있으면 수행안함 (값이 있으므로)
			int num = connectUsers.get(roomId);
			connectUsers.put(roomId, num+1);
		} else if (Objects.equals(status, "Disconnect")) {
			//connectUsers.putIfAbsent(roomId, 0);
			int num = connectUsers.get(roomId);
			connectUsers.put(roomId, num-1);
		}
		log.info("현재 인원 : " + connectUsers.get(roomId));
	}


	//채팅방 불러오기
	public List<ChatRoomDto> findAllRoom() {
		List<ChatRoomDto> chatRoomDtos = new ArrayList<>();
		List<Chat> all = chatRepository.findAll();
		try {
			for (int i = 0; i < all.size(); i++) {
				User user = userRepository.findById(all.get(i).getIdx()).orElseThrow(UserNotFoundException::new);
				chatRoomDtos.add(ChatRoomDto.of(all.get(i), user));
			}
		} catch (NullPointerException e) {
			log.info(" [현재 채팅방 db 없음!] " + e);
		}
		return chatRoomDtos;
	}

	// 삭제 후 재 접속 막기
	public boolean getRoomInfo(String roomId) {
		return chatRepository.existsByRoomId(roomId);
	}

	//채팅방 생성
	public ChatRoomDto createRoom(String nickname) {
		System.out.println("alksdjflajdflad");
		User user = userRepository.findByNickname(nickname).orElseThrow(UserNotFoundException::new);
		ChatRoomDto chatRoom = new ChatRoomDto();
		if (!chatRepository.existsByUserId(user.getId())) {
			chatRoom = ChatRoomDto.create(nickname);
			ChatRoomMap.getInstance().getChatRooms().put(chatRoom.getRoomId(), chatRoom);
			Chat chat = new Chat(chatRoom.getRoomId(), user);
			log.info("Service chat :  " + chat);
			chatRepository.save(chat);
			return chatRoom;
		} else {
			Optional<Chat> findChat = chatRepository.findByUserId(user.getId());
			return ChatRoomDto.of(findChat.get(), user);
		}
	}

	//채팅방 하나 불러오기
	public ChatRoomDto roomOne(String nickname) throws CustomException {
		User user = userRepository.findByNickname(nickname).orElseThrow(UserNotFoundException::new);
		Chat chat = chatRepository.findByUserId(user.getId()).orElseThrow(ChatRoomNotFoundException::new);
		return ChatRoomDto.of(chat, user);
	}

	public void deleteRoom(String roomId) {
		Timer t = new Timer(true);
		TimerTask task = new MyTimeTask(chatRepository, roomId, chatUploadLocation);
		t.schedule(task, 300000);
		log.warn("5분뒤에 삭제 됩니다.");
	}

	public ChatMessage chatAlarm(String sender, String roomId){
		ChatMessage chatMessage = new ChatMessage();
		if (Objects.equals(sender, "admin") && connectUsers.get(roomId) ==1){
			chatMessage.setRoomId(roomId);
			chatMessage.setSender(sender);
			chatMessage.setMessage("고객센터에 문의한 글에 답글이 달렸습니다.");
		} else if (!Objects.equals(sender, "admin") && connectUsers.get(roomId) == 1) {
			chatMessage.setRoomId(roomId);
			chatMessage.setSender(sender);
			chatMessage.setMessage(sender + " 님이 답을 기다리고 있습니다.");

		}
		return chatMessage;
	}

	public void saveFile(ChatMessage chatMessage) { // 파일 저장
		JSONObject json = new JSONObject();
		json.put("roomId", chatMessage.getRoomId());
		json.put("type", chatMessage.getType().toString());
		json.put("sender", chatMessage.getSender());
		json.put("message", chatMessage.getMessage());
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json1 = gson.toJson(json);
		try {
			FileWriter file = new FileWriter(chatUploadLocation + "/" + chatMessage.getRoomId() + ".txt", true);
			File file1 = new File(chatUploadLocation + "/" + chatMessage.getRoomId() + ".txt");
			if (file1.exists() && file1.length() == 0) {
				file.write(json1);
				chatAlarm(chatMessage.getSender(), chatMessage.getRoomId());
			} else {
				file.write("," + json1);
			}
			file.flush();
			file.close(); // 연결 끊기
		} catch (IOException e) {
			log.error("[error] " + e);
		}
	}

	public Object readFile(String roomId) {
		try {
			//FileReader reader = new FileReader(chatUploadLocation + "/" + roomName + "-" + roomId + ".txt");
			String str = Files.readString(Paths.get(chatUploadLocation + "/" + roomId + ".txt"));
			JSONParser parser = new JSONParser();
			//Object object = parser.parse(reader);
			Object obj = parser.parse("[" + str + "]");
			//reader.close();
			return obj;
		} catch (NoSuchFileException e) {
			throw new FileNotFoundException();
		} catch (IOException | ParseException e) {
			log.error("[error] " + e);
			return null;
		}
	}
}