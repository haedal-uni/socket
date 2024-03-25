package com.dalcho.adme.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResultDto {

    private boolean success;

    private int code;

    private String msg;

    private String role_check;

}
