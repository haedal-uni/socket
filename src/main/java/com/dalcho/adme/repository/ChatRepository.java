package com.dalcho.adme.repository;

import com.dalcho.adme.dto.ChatRoomDto;
import com.dalcho.adme.dto.ChatRoomMap;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;


@Repository
public class ChatRepository {
	private Map<String, ChatRoomDto> chatRoomDTOMap;
	@PostConstruct
	//의존관게 주입완료되면 실행되는 코드
	private void init() {
		chatRoomDTOMap = new LinkedHashMap<>();
	}

	//채팅방 불러오기
	public List<ChatRoomDto> findAllRoom() {
		//채팅방 최근 생성 순으로 반환
		List<ChatRoomDto> result = new ArrayList<>(ChatRoomMap.getInstance().getChatRooms().values());
		List<ChatRoomDto> result1 = new ArrayList<>(chatRoomDTOMap.values());
		System.out.println("result1 :  + " + result1);
		Collections.reverse(result);
		return result;
	}

	//채팅방 하나 불러오기
	public ChatRoomDto findById(String roomId) {
		System.out.println("chatRoomDTOMap.get(roomId);  " +chatRoomDTOMap.get(roomId));

		return ChatRoomMap.getInstance().getChatRooms().get(roomId);
	}

	//채팅방 생성
	public ChatRoomDto createRoom(String name) {
		ChatRoomDto chatRoom = ChatRoomDto.create(name);
		//chatRoomDTOMap.put(chatRoom.getRoomId(), chatRoom);

		ChatRoomMap.getInstance().getChatRooms().put(chatRoom.getRoomId(), chatRoom);
		return chatRoom;
	}

	// maxUserCnt 에 따른 채팅방 입장 여부
	public boolean chkRoomUserCnt(String roomId) {
		ChatRoomDto room = ChatRoomMap.getInstance().getChatRooms().get(roomId);
		if (room.getUserCount() + 1 > room.getMaxUserCnt()) {
			return false;
		}
		return true;
	}
}
// ChatRoomMap.getInstance().getChatRooms() == chatRooms