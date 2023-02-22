package com.dalcho.adme.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Kakao {
	public Kakao(String username, String password, String email, UserRole role, Long kakaoIdx) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.role = role;
		this.kakaoIdx = kakaoIdx;
	}

	// ID가 자동으로 생성 및 증가합니다.
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "kakao_id")
	private Long id;
	// 반드시 값을 가지도록 합니다.
	@Column(nullable = false)
	private String username;
	@Column(nullable = false)
	private String password;
	@Column(nullable = false)
	private String email;
	@Column(nullable = false)
	@Enumerated(value = EnumType.STRING)
	private UserRole role;
	@Column(nullable = false)
	private Long kakaoIdx;
}
