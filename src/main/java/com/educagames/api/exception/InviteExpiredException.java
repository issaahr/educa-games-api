package com.educagames.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class InviteExpiredException extends BaseException {
    public InviteExpiredException(String message) {
        super(message);
    }
}
