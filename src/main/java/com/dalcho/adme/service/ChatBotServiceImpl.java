package com.dalcho.adme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.java.com.dalcho.adme.dto.ChatMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.websocket.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatBotServiceImpl extends TextWebSocketHandler {
	private static List<Session> sessionUsers = Collections.synchronizedList(new ArrayList<>());
	private static ChatMessage chatMessage = new ChatMessage();
	public int userCount() {
		chatMessage.setUserCount(chatMessage.getUserCount()+1);
		log.info("userCount : " + sessionUsers.size());
		log.info("chatMessage.getUserCount : " + chatMessage.getUserCount());
		return sessionUsers.size();
	}

	@OnOpen
	public void open(Session newUser) throws IOException {
		sessionUsers.add(newUser);
		log.info("현재 접속자 수 : " + sessionUsers.size());
		userCount();
	}

	@OnMessage // 사용자로부터 메시지를 받았을 때, 실행된다.
	public void onMessage(Session receiveSession, String msg) throws IOException {
		for (int i = 0; i < sessionUsers.size(); i++) {
			if (!receiveSession.getId().equals(sessionUsers.get(i).getId())) {
				try {
					sessionUsers.get(i).getBasicRemote().sendText("$" + msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					sessionUsers.get(i).getBasicRemote().sendText(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 파일를 저장하는 함수
	private void saveFile(String id, String message) {
		// 메시지 내용
		String msg = id + ":  " + message + "\n";
		// 파일을 저장한다.
		try (FileOutputStream stream = new FileOutputStream("${part4.upload.path}", true)) {
			stream.write(msg.getBytes("UTF-8"));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}

	@OnClose
	public void onClose(Session nowUser, CloseReason closeReason) {
		sessionUsers.remove(nowUser);
	}

	//에러 발생시
	@OnError
	public void onError(Session session, Throwable e) {
		e.printStackTrace();
	}
}