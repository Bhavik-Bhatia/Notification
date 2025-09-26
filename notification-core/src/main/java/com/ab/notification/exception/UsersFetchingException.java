package com.ab.notification.exception;

import lombok.Getter;

@Getter
public class UsersFetchingException extends AppException {

    public UsersFetchingException(String message) {
        super(message);
    }
}
