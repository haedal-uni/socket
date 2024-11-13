package com.dalcho.adme.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LastMessage {
    private String roomId;
    private int adminChat;
    private int userChat;
    private String message;
    private String day;
    private String time;

    @Builder
    public LastMessage(String roomId, int adminChat, int userChat, String message, String day, String time) {
        this.roomId = roomId;
        this.adminChat = adminChat;
        this.userChat = userChat;
        this.message = message;
        this.day = day;
        this.time = time;
    }

    public static LastMessage of(ChatMessage chatMessage, int adminChat, int userChat, String day, String time){
        return LastMessage.builder()
                .roomId(chatMessage.getRoomId())
                .adminChat(adminChat)
                .userChat(userChat)
                .message(chatMessage.getMessage())
                .day(day)
                .time(time)
                .build();
    }
}
