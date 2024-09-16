package com.dalcho.adme.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private LocalDate statDate; // 통계 날짜

    @Column
    private Long loggedInUsers; // 로그인한 사용자 수

    @Column
    private Long chatUsers;  // 채팅 참여 사용자 수

    @Column
    private Double participationRate;  // 참여 비율

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public Stats(LocalDate date, Long users, Long chat, Double participationRate ){
        this.statDate = date;
        this.loggedInUsers = users;
        this.chatUsers = chat;
        this.participationRate = participationRate;
    }
}

