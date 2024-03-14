package com.dalcho.adme.controller;

import com.dalcho.adme.dto.user.SignInRequestDto;
import com.dalcho.adme.dto.user.SignInResultDto;
import com.dalcho.adme.dto.user.SignUpRequestDto;
import com.dalcho.adme.dto.user.SignUpResultDto;
import com.dalcho.adme.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SignController {
    private final SignService signService;

    @PostMapping(value = "/sign-up")
    public SignUpResultDto signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        SignUpResultDto signUpResultDto = signService.signUp(signUpRequestDto);

        return signUpResultDto;
    }

    @PostMapping(value = "/sign-in")
    public SignInResultDto signIn(@RequestBody SignInRequestDto signInRequestDto) throws RuntimeException {
        SignInResultDto signInResultDto = signService.signIn(signInRequestDto);

        if (signInResultDto.getCode() == 0) {
            log.info("[signIn] 정상적으로 로그인 되었습니다. id : {}", signInRequestDto.getNickname());
        }
        return signInResultDto;
    }
}