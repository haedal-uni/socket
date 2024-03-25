package com.dalcho.adme.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignInResultDto extends SignUpResultDto {
    private String token;
    private String username;

    @Builder
    public SignInResultDto(boolean success, int code, String msg, String token, String role_check, String username) {
        super(success, code, msg, role_check);
        this.token = token;
        this.username = username;
    }
}
