package com.dalcho.adme.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
public class SignInRequestDto {
    private String nickname;
    private String password;
}
