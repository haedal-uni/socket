package com.dalcho.adme.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class User implements UserDetails {
	public static final String DEFAULT_PROFILE_IMG_PATH = "images/default-profile.png";

	@GeneratedValue(strategy = IDENTITY)
	@Id
	@Column(name = "user_id")
	private Long id;

	@Column(nullable = false)
	private String username;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	@Enumerated(value = EnumType.STRING)
	private UserRole role;

	@Column(nullable = false)
	private boolean enabled = true; // 1

	@Column(nullable = false)
	private String socialId;
	private String social;

	private String profile = DEFAULT_PROFILE_IMG_PATH;

	@OneToOne(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL) //고아 객체 제거 기능을 활성화
	@JsonIgnore
	@ToString.Exclude
	private Chat chat;

	@Builder // UserMapper와 연결
	public User(String socialId, String email, String password, UserRole role, String username, String nickname, String social) {
		this.socialId = socialId;
		this.email = email;
		this.password = password;
		this.username = username;
		this.nickname = nickname;
		this.role = role == null ? UserRole.USER : role;
		this.profile = DEFAULT_PROFILE_IMG_PATH;
		this.social = social;
	}

	public void addChat(Chat chat){
		chat.addUser(this);
		this.chat = chat;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public String getUsername() {
		return null;
	}

	@Override
	public boolean isAccountNonExpired() { // 사용자 계정 만료 여부
		return false;
	}

	@Override
	public boolean isAccountNonLocked() { // 사용자 잠금 여부
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() { // 비밀번호 만료 여부
		return false;
	}

	@Override
	public boolean isEnabled() { // 사용자 활성화 여부
		return this.enabled;
	}
}
