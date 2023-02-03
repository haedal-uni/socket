package com.dalcho.adme.service;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.ChatResponse;
import com.dalcho.adme.dto.ChatResponse.ResponseType;
import com.dalcho.adme.dto.ChatRoomDto;
import com.dalcho.adme.dto.ChatRoomMap;
import com.dalcho.adme.exception.CustomException;
import com.dalcho.adme.exception.notfound.SocketNotFoundException;
import com.dalcho.adme.model.Socket;
import com.dalcho.adme.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatServiceImpl {
	private final ChatRepository chatRepository;
	private Map<ChatRoomMap, DeferredResult<ChatResponse>> waitingUsers;
	// {key : websocket session id, value : chat room id}
	private Map<String, DeferredResult<ChatResponse>> watingQueue;
	private ReentrantReadWriteLock lock;
	private ReentrantLock locks;
	private final SimpMessagingTemplate template;
	private final Queue<ChatMessage> queue = new LinkedList<>();

	@PostConstruct // @PostConstruct는 의존성 주입이 이루어진 후 초기화를 수행하는 메서드
	private void setUp() {
		this.waitingUsers = new LinkedHashMap<>();
		this.watingQueue = new LinkedHashMap<>();
		//this.lock = new ReentrantLock();
		this.lock = new ReentrantReadWriteLock();
	}

	@Async("asyncThreadPool")
	public void addUser(ChatRoomMap request, DeferredResult<ChatResponse> deferredResult) throws IllegalStateException {
		log.info("## Join chat room request. {}[{}]", Thread.currentThread().getName(), Thread.currentThread().getId());
		if (request == null || deferredResult == null) {
			return;
		} else {
			try {
				//lock.lock();
				lock.writeLock().lock();
				waitingUsers.put(request, deferredResult);
				watingQueue.put(request.getSessionId(), deferredResult);
			} finally {
				//lock.unlock();
				lock.writeLock().unlock();
			}
		}
	}

	public void timeout(ChatRoomMap user, String roomId) {
		if (watingQueue.size() ==1) {
			try {
				//lock.lock();
				lock.writeLock().lock();
				setJoinResult(new ChatResponse(ResponseType.TIMEOUT, roomId, user.getSessionId()));
			} finally {
				//lock.unlock();
				lock.writeLock().unlock();
			}
		}

	}

	private void setJoinResult(ChatResponse response) {
		if (response != null) {
			template.convertAndSend("/topic/public/" + response.getRoomId(), response);
		}
	}

	//채팅방 불러오기
	public List<ChatRoomDto> findAllRoom() {
		List<ChatRoomDto> chatRoomDtos = new ArrayList<>();
		List<Socket> all = chatRepository.findAll();
		try {
			for (int i = 0; i < all.size(); i++) {
				chatRoomDtos.add(ChatRoomDto.of(all.get(i)));
			}
		} catch (NullPointerException e) {
			throw new RuntimeException("data 없음! ");
		}
		return chatRoomDtos;
	}

	//채팅방 하나 불러오기
	public boolean getRoomInfo(String roomId) {
		return chatRepository.existsByRoomId(roomId);
	}

	//채팅방 생성
	public ChatRoomDto createRoom(String nickname) {
		ChatRoomDto chatRoom = new ChatRoomDto();
		if (!chatRepository.existsByNickname(nickname)) {
			chatRoom = ChatRoomDto.create(nickname);
			ChatRoomMap.getInstance().getChatRooms().put(chatRoom.getRoomId(), chatRoom);
			Socket socket = new Socket(chatRoom.getRoomId(), nickname);
			log.info("Service socket :  " + socket);
			chatRepository.save(socket);
			return chatRoom;
		} else {
			Optional<Socket> byNickname = chatRepository.findByNickname(nickname);
			return ChatRoomDto.of(byNickname.get());
		}
	}

	public ChatRoomDto roomOne(String nickname) throws CustomException {
		Socket socket = chatRepository.findByNickname(nickname).orElseThrow(SocketNotFoundException::new);
		return ChatRoomDto.of(socket);
	}

	public void deleteRoom(String roomId) {
		Timer t = new Timer(true);
		TimerTask task = new MyTimeTask(chatRepository, roomId);
		t.schedule(task, 300000);
		log.info("5분뒤에 삭제 됩니다.");
	}
}