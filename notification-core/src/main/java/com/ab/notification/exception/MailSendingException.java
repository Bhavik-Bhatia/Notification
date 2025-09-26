package com.ab.notification.exception;

import lombok.Getter;

@Getter
public class MailSendingException extends AppException {

    public MailSendingException(String message) {
        super(message);
    }
}
