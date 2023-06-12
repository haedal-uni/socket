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
	// {key : nickname, value : chat room id}
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
		long startTime = System.currentTimeMillis();
		log.info("## Join chat room request. {}[{}]", Thread.currentThread().getName(), Thread.currentThread().getId());
		if (request == null || deferredResult == null) {
			return;
		}
		try {
			lock.writeLock().lock();
			waitingUsers.put(request, deferredResult); // nickname, roomId
		} catch (TaskRejectedException e){
			log.warn(String.valueOf(e));
		}
		finally {
			lock.writeLock().unlock();
			establishChatRoom();
		}
		long endTime = System.currentTimeMillis();
		log.info(String.format("코드 실행 시간 : %20dms", endTime - startTime));
	}

	public void timeout(ChatRoomMap chatRoomMap) {
		log.info("timeout");
		if (watingQueue.size() < 2) {
			try {
				lock.writeLock().lock();
				setJoinResult(waitingUsers.remove(chatRoomMap), new EveryChatResponse(EveryChatResponse.ResponseType.TIMEOUT, null, chatRoomMap.getNickname()));
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
			}else{
				log.info("[random chat] chat start !!");
				// 대기 큐에 2명 이상
				Iterator<ChatRoomMap> itr = waitingUsers.keySet().iterator();
				// next() : Iterator가 자신이 가리키는 데이터저장소에서 현재위치를 순차적으로 하나 증가해서 이동
				ChatRoomMap user1 = itr.next();
				ChatRoomMap user2 = itr.next();
				String uuid = "aaaa" + UUID.randomUUID().toString();
				DeferredResult<EveryChatResponse> user1Result = waitingUsers.remove(user1);
				DeferredResult<EveryChatResponse> user2Result = waitingUsers.remove(user2);
				//UUID로 채팅방 이름 생성 + Success (+ 채팅방 이름 포함)
				user1Result.setResult(new EveryChatResponse(EveryChatResponse.ResponseType.SUCCESS, uuid, user1.getNickname()));
				user2Result.setResult(new EveryChatResponse(EveryChatResponse.ResponseType.SUCCESS, uuid, user2.getNickname()));
			}
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

	public void connectUser(String roomId, String nickname) {
		connectedUsers.put(nickname, roomId);
	}

	public void disconnectUser(String nickname, String sessionRoomId) {
		String roomId = connectedUsers.get(nickname);
		if (!Objects.equals(roomId, sessionRoomId)) {
			log.error(" [error] : Map에 저장된 roomId와 session에 저장된 roomId가 같지 않습니다. ");
			log.info(" [roomId] : " + roomId + "  , [sessionRoomId] : " + sessionRoomId);
		}
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setType(ChatMessage.MessageType.LEAVE);
		chatMessage.setSender(nickname);
		chatMessage.setRoomId(roomId);
		sendMessage(roomId, chatMessage);
		connectedUsers.remove(nickname, roomId);
	}

	private String getDestination(String roomId) { // 채팅방 url 주소
		return "/every-chat/" + roomId;
	}

	public void cancelChatRoom(ChatRoomMap chatRoomMap) {
		log.info(" = = = = = = = cancelChatRoom = = = = = = = ");
		try {
			lock.writeLock().lock();
			setJoinResult(waitingUsers.remove(chatRoomMap), new EveryChatResponse(EveryChatResponse.ResponseType.CANCEL, null, chatRoomMap.getNickname()));
		} finally {
			lock.writeLock().unlock();
		}
	}
	/*
	writeLock()을 사용하여 쓰기 락(lock)을 획득한다. 이는 동시에 여러 스레드가 cancelChatRoom() 메소드를 호출할 때 동기화를 보장하기 위해 사용된다.
	waitingUsers에서 chatRoomMap을 제거하고, 해당 chatRoomMap을 사용하여 EveryChatResponse 객체를 생성한다.
	setJoinResult()를 호출하여 EveryChatResponse 객체를 결과로 설정한다. 이 메소드는 어디에 정의되어 있는지에 따라 다른 동작을 수행할 수 있다.
	writeLock()을 해제한다.
	 */
}
