package com.ab.notification.exception;


import lombok.Getter;
import java.util.UUID;

@Getter
public class AppException extends Exception {

    private final String traceId;

    public AppException(String message) {
        super(message);
        this.traceId = UUID.randomUUID().toString();
    }
}
