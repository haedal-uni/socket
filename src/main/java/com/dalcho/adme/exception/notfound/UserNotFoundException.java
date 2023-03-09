package com.dalcho.adme.exception.notfound;

import com.dalcho.adme.exception.CustomException;
import com.dalcho.adme.exception.ErrorCode;

public class UserNotFoundException extends CustomException {
	public UserNotFoundException() {
		super(ErrorCode.USER_NOT_FOUND);
	}
}
