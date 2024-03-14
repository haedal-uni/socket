package com.dalcho.adme.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 관리자 암호입니다."),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "잘못된 패스워드입니다."),
	INVALID_NICKNAME(HttpStatus.UNAUTHORIZED, "사용할 수 없는 nickname 입니다."),
	INVALID_PATTERN(HttpStatus.UNAUTHORIZED, "잘못된 형식의 패턴입니다."),
	USER_DUPLICATE_ID(HttpStatus.CONFLICT, "중복된 Nickname 이 존재합니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER가 없습니다."),
	CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 만들어주세요."),
	DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 에러"),
	FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방 파일을 찾을 수 없습니다."),
	BAD_CONSTANT(HttpStatus.BAD_GATEWAY, "잘못된 인자입니다.");
	private final HttpStatus httpStatus;
	private final String message;

	ErrorCode(HttpStatus httpStatus, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
	}
}

