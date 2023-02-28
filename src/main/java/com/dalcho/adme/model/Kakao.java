package com.dalcho.adme.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Kakao implements UserDetails {
	public static final String DEFAULT_PROFILE_IMG_PATH = "images/default-profile.png";

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "kakao_id")
	private Long id;
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

	private String profile = DEFAULT_PROFILE_IMG_PATH;

	@Builder // UserMapper와 연결
	public Kakao(Long id, String email, String password, UserRole role, String nickname) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.nickname = nickname;

		this.role = role == null ? UserRole.USER : role;
		this.profile = DEFAULT_PROFILE_IMG_PATH;
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
	public boolean isAccountNonExpired() {
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
}
