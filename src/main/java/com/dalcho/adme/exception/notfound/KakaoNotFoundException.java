package com.dalcho.adme.exception.notfound;

import com.dalcho.adme.exception.CustomException;
import com.dalcho.adme.exception.ErrorCode;

public class KakaoNotFoundException extends CustomException {
	public KakaoNotFoundException() {
		super(ErrorCode.KAKAO_NOT_FOUND);
	}
}
