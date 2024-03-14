package com.dalcho.adme.service;

import com.dalcho.adme.dto.user.SignInRequestDto;
import com.dalcho.adme.dto.user.SignInResultDto;
import com.dalcho.adme.dto.user.SignUpRequestDto;
import com.dalcho.adme.dto.user.SignUpResultDto;

public interface SignService {
    SignUpResultDto signUp(SignUpRequestDto signUpRequestDto);

    SignInResultDto signIn(SignInRequestDto signInRequestDto) throws RuntimeException;

}
