package com.dalcho.adme.dto.user;

import lombok.Builder;
import lombok.Getter;

import javax.swing.plaf.PanelUI;

// stomp
@Getter
public class LoginInfo {
	private String name;
	private String token;

	@Builder
	public LoginInfo(String name, String token){
		this.name = name;
		this.token = token;
	}
}
