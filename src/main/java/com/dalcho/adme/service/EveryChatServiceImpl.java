package com.dalcho.adme.service;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.EveryChatResponse;
import com.dalcho.adme.dto.ChatRoomMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@RequiredArgsConstructor
@Service
public class EveryChatServiceImpl {
	private Map<ChatRoomMap, DeferredResult<EveryChatResponse>> waitingUsers;
	// {key : websocket session id, value : chat room id}
	private Map<String, DeferredResult<EveryChatResponse>> watingQueue;
	private Map<String, String> connectedUsers;
	private ReentrantReadWriteLock lock;
	private final SimpMessagingTemplate template;

	@PostConstruct // @PostConstruct는 의존성 주입이 이루어진 후 초기화를 수행하는 메서드
	private void setUp() { // 안그러면 NullPointerException
		this.waitingUsers = new LinkedHashMap<>();
		this.watingQueue = new LinkedHashMap<>();
		this.connectedUsers = new HashMap<>();
		//this.lock = new ReentrantLock();
		this.lock = new ReentrantReadWriteLock();
	}

	@Async("executor") // 비동기
	public void addUser(ChatRoomMap request, DeferredResult<EveryChatResponse> deferredResult) throws IllegalStateException {
		log.info("## Join chat room request. {}[{}]", Thread.currentThread().getName(), Thread.currentThread().getId());
		if (request == null || deferredResult == null) {
			return;
		}
		try {
			lock.writeLock().lock();
			waitingUsers.put(request, deferredResult); // sessionId, roomId
		} catch (TaskRejectedException e){
			log.warn(String.valueOf(e));
		}
		finally {
			lock.writeLock().unlock();
			establishChatRoom();
		}
	}

	public void timeout(ChatRoomMap chatRoomMap) {
		if (watingQueue.size() < 2) {
			try {
				lock.writeLock().lock();
				setJoinResult(waitingUsers.remove(chatRoomMap), new EveryChatResponse(EveryChatResponse.ResponseType.TIMEOUT, null, chatRoomMap.getSessionId()));
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	public void establishChatRoom() { // 채팅방 시작하면 대기타는중
		try {
			log.debug("Current waiting users : " + waitingUsers.size());
			lock.readLock().lock();
			if (waitingUsers.size() < 2) { // 대기 큐에 2명 미만이면 대기
				return;
			}
			// 대기 큐에 2명 이상
			Iterator<ChatRoomMap> itr = waitingUsers.keySet().iterator();
			// next() : Iterator가 자신이 가리키는 데이터저장소에서 현재위치를 순차적으로 하나 증가해서 이동
			ChatRoomMap user1 = itr.next();
			ChatRoomMap user2 = itr.next();
			String uuid = "aaaa" + UUID.randomUUID().toString();
			DeferredResult<EveryChatResponse> user1Result = waitingUsers.remove(user1);
			DeferredResult<EveryChatResponse> user2Result = waitingUsers.remove(user2);
			//UUID로 채팅방 이름 생성 + Success (+ 채팅방 이름 포함)
			user1Result.setResult(new EveryChatResponse(EveryChatResponse.ResponseType.SUCCESS, uuid, user1.getSessionId()));
			user2Result.setResult(new EveryChatResponse(EveryChatResponse.ResponseType.SUCCESS, uuid, user2.getSessionId()));
		} catch (Exception e) {
			log.error("Exception occur while checking waiting users", e);
		} finally {
			lock.readLock().unlock();
		}
	}

	public void setJoinResult(DeferredResult<EveryChatResponse> result, EveryChatResponse response) {
		if (result != null) {
			result.setResult(response);
		}
	}

	public void sendMessage(String roomId, ChatMessage chatMessage) {
		String destination = getDestination(roomId); // 주소
		template.convertAndSend(destination, chatMessage);
	}

	public void connectUser(String roomId, String websocketSessionId) {
		connectedUsers.put(websocketSessionId, roomId);
	}

	public void disconnectUser(String websocketSessionId, String sessionRoomId,	String username) {
		String roomId = connectedUsers.get(websocketSessionId);
		if (!Objects.equals(roomId, sessionRoomId)) {
			log.error(" [error] : Map에 저장된 roomId와 session에 저장된 roomId가 같지 않습니다. ");
			log.info(" [roomId] : " + roomId + "  , [sessionRoomId] : " + sessionRoomId);
		}
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setType(ChatMessage.MessageType.LEAVE);
		chatMessage.setSender(username);
		chatMessage.setRoomId(roomId);
		sendMessage(roomId, chatMessage);
		connectedUsers.remove(websocketSessionId, roomId);
	}

	private String getDestination(String roomId) { // 채팅방 url 주소
		return "/every-chat/" + roomId;
	}

	public void cancelChatRoom(ChatRoomMap chatRoomMap) {
		log.info(" = = = = = = = cancelChatRoom = = = = = = = ");
		try {
			lock.writeLock().lock();
			setJoinResult(waitingUsers.remove(chatRoomMap), new EveryChatResponse(EveryChatResponse.ResponseType.CANCEL, null, chatRoomMap.getSessionId()));
		} finally {
			lock.writeLock().unlock();
		}
	}
}
