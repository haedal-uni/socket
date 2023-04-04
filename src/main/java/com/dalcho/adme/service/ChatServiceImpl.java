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
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
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
	private Map<String, Integer> adminChat;
	private Map<String, Integer> userChat;


	@Value("${spring.servlet.multipart.location}")
	private String chatUploadLocation;
	private final UserRepository userRepository;

	@PostConstruct // @PostConstruct는 의존성 주입이 이루어진 후 초기화를 수행하는 메서드
	private void setUp() { // 안그러면 NullPointerException
		this.connectUsers = new HashMap<>();
		this.adminChat = new HashMap<>();
		this.userChat = new HashMap<>();
	}

	public void connectUser(String status, String roomId, ChatMessage chatMessage) {
		if (Objects.equals(status, "Connect")){
			connectUsers.putIfAbsent(roomId, 0); // 값이 없으면 이걸 수행하고 있으면 수행안함 (값이 있으므로)
			int num = connectUsers.get(roomId);
			connectUsers.put(roomId, (num+1));
			saveFile(chatMessage);
		} else if (Objects.equals(status, "Disconnect")) {
			int num = connectUsers.get(roomId);
			connectUsers.put(roomId, (num-1));
		}
		log.info("현재 인원 : " + connectUsers.get(roomId));
	}


	//채팅방 불러오기
	public List<ChatRoomDto> findAllRoom() {
		List<ChatRoomDto> chatRoomDtos = new ArrayList<>();
		List<Chat> all = chatRepository.findAll();
		try {
			for (int i = 0; i < all.size(); i++) {
				User user = userRepository.findById(all.get(i).getUser().getId()).orElseThrow(UserNotFoundException::new);
				chatRoomDtos.add(ChatRoomDto.of(all.get(i), user, lastLine(all.get(i).getRoomId())));
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
			return ChatRoomDto.of(findChat.get(), user, lastLine(findChat.get().getRoomId()));
		}
	}

	//채팅방 하나 불러오기
	public ChatRoomDto roomOne(String nickname) throws CustomException {
		User user = userRepository.findByNickname(nickname).orElseThrow(UserNotFoundException::new);
		Chat chat = chatRepository.findByUserId(user.getId()).orElseThrow(ChatRoomNotFoundException::new);
		return ChatRoomDto.of(chat, user, new ArrayList<>());
	}

	public void deleteRoom(String roomId) {
		Timer t = new Timer(true);
		TimerTask task = new MyTimeTask(chatRepository, roomId, chatUploadLocation);
		t.schedule(task, 300000);
		log.warn("5분뒤에 삭제 됩니다.");
	}

	public ChatMessage chatAlarm(String sender, String roomId){
		ChatMessage chatMessage = new ChatMessage();
		if (Objects.equals(sender, "admin") && connectUsers.get(roomId) == 1){
			chatMessage.setRoomId(roomId);
			chatMessage.setSender(sender);
			chatMessage.setMessage("고객센터에 문의한 글에 답글이 달렸습니다.");
			return chatMessage;
		} else if (!Objects.equals(sender, "admin") && connectUsers.get(roomId) == 1) {
			chatMessage.setRoomId(roomId);
			chatMessage.setSender(sender);
			chatMessage.setMessage(sender + " 님이 답을 기다리고 있습니다.");
			return chatMessage;
		}else {
			return chatMessage;
		}
	}

	public void saveFile(ChatMessage chatMessage) { // 파일 저장
		if (connectUsers.get(chatMessage.getRoomId())!=0){
			if ((chatMessage.getType().toString()).equals("JOIN")){
				reset(chatMessage.getSender(), chatMessage.getRoomId());
			} else {
				countChat(chatMessage.getSender(), chatMessage.getRoomId());
			}
		}
		JSONObject json = new JSONObject();
		json.put("roomId", chatMessage.getRoomId());
		json.put("type", chatMessage.getType().toString());
		json.put("sender", chatMessage.getSender());
		json.put("message", chatMessage.getMessage());
		json.put("adminChat", adminChat.get(chatMessage.getRoomId()));
		json.put("userChat", userChat.get(chatMessage.getRoomId()));
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

	public void reset(String sender, String roomId){
		if (sender.equals("admin")){
			adminChat.putIfAbsent(roomId, 0);
			userChat.putIfAbsent(roomId, 0);
			adminChat.put(roomId,0);
		} else {
			userChat.putIfAbsent(roomId, 0);
			adminChat.putIfAbsent(roomId, 0);
			userChat.put(roomId,0);
		}
	}

	public void countChat(String sender, String roomId){
		if(sender.equals("admin")) {
			userChat.putIfAbsent(roomId, 0);
			int num = userChat.get(roomId);
			userChat.put(roomId, num+1);
			adminChat.put(roomId,0);
		}
		else {
			adminChat.putIfAbsent(roomId, 0);
			int num = adminChat.get(roomId);
			adminChat.put(roomId, num+1);
			userChat.put(roomId,0);
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

	public List lastLine(String roomId) {
		File file1 = new File(chatUploadLocation + "/" + roomId + ".txt");
		try{
			// 1. ReversedLinesFileReader  준비
			ReversedLinesFileReader reader
					= new ReversedLinesFileReader(file1, Charset.forName("UTF-8"));

			// 2. 뒤에서 7줄 읽기
			List<String> lines = reader.readLines(7);
/*
			RandomAccessFile file = new RandomAccessFile(chatUploadLocation + "/" + roomId + ".txt", "r");
			StringBuffer lastLine = new StringBuffer();
			int lineCount = 7;
			// 2. 전체 파일 길이
			long fileLength = file.length();

			// 3. 포인터를 이용하여 뒤에서부터 앞으로 데이터를 읽는다.
			for (long pointer = fileLength - 1; pointer >= 0; pointer--) {

				// 3.1. pointer를 읽을 글자 앞으로 옮긴다.
				file.seek(pointer);

				// 3.2. pointer 위치의 글자를 읽는다.
				char c = (char) file.read();

				// 3.3. 줄바꿈이 7번(lineCount) 나타나면 더 이상 글자를 읽지 않는다.
				if (c == '\n') {
					lineCount--;
					if (lineCount == 0) {
						break;
					}
				}
				// 3.4. 결과 문자열의 앞에 읽어온 글자(c)를 붙여준다.
				lastLine.insert(0, c);

			}
			int adminChat = lastLine.indexOf("adminChat"); //lastLine.substring(adminChat+12, adminChat+13)
			int userChat = lastLine.indexOf("userChat"); //lastLine.substring(userChat+11, userChat+12)
*/
			List<String> chat = new ArrayList<>();
			chat.add(lines.get(6).substring(15, lines.get(6).length()-1)); // adminChat
			chat.add(lines.get(4).substring(14, lines.get(4).length()-1)); // userChat
			chat.add(lines.get(2).substring(14, lines.get(2).length()-2)); // message
			return chat;
			// 4. 결과 출력
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

}