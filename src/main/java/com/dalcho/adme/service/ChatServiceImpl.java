package com.dalcho.adme.service;

import com.dalcho.adme.dto.ChatRoomDto;
import com.dalcho.adme.dto.ChatRoomMap;
import com.dalcho.adme.model.Socket;
import com.dalcho.adme.repository.SocketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatServiceImpl {
	private final SocketRepository socketRepository;
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
		Collections.reverse(result);
		return result;
	}

	//채팅방 하나 불러오기
	public ChatRoomDto findById(String roomId) {
		return ChatRoomMap.getInstance().getChatRooms().get(roomId);
	}

	//채팅방 생성
	public ChatRoomDto createRoom(String nickname) {
		ChatRoomDto chatRoom = null;
		if (!socketRepository.existsByNickname(nickname)) {
			chatRoom = ChatRoomDto.create(nickname); // name으로 새로 id를 만드는 코드
			ChatRoomMap.getInstance().getChatRooms().put(chatRoom.getRoomId(), chatRoom);
			Socket socket = new Socket(chatRoom.getRoomId(), nickname);
			log.info("Service socket :  " + socket);
			socketRepository.save(socket);
		}
		else{
			Optional<Socket> byNickname = socketRepository.findByNickname(nickname);
			chatRoom.toEntity(byNickname.get().getRoomId(), byNickname.get().getNickname());
		}
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