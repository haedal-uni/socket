package com.dalcho.adme.dto.user;

import lombok.Getter;

@Getter
public class SignUpRequestDto {

    private String nickname;

    private String password;

    private String name;

    private String email;

    private boolean admin = false;

    private String adminToken = "";
}
