package com.ab.notification.exception;


import lombok.Getter;

import java.util.UUID;

@Getter
public class AppException extends Exception {

    private final String traceId;
    private final String errorCode;

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode.name();
        this.traceId = UUID.randomUUID().toString();
    }
}
