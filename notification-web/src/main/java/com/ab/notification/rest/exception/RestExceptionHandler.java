package com.ab.notification.rest.exception;


import com.ab.notification.exception.ErrorCode;
import com.ab.notification.exception.ErrorResponse;
import com.ab.notification.exception.MailSendingException;
import com.ab.notification.exception.UsersFetchingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(UsersFetchingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUsersFetchingException(UsersFetchingException ex) {
        return new ErrorResponse(ErrorCode.USERS_FETCHING_ERROR.name(), ex.getMessage(), ex.getTraceId());
    }

    @ExceptionHandler(MailSendingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleMailSendingException(MailSendingException ex) {
        return new ErrorResponse(ErrorCode.MAIL_SENDING_ERROR.name(), ex.getMessage(), ex.getTraceId());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneralException(Exception ex) {
        return new ErrorResponse(ErrorCode.INTERNAL_ERROR.name(), ex.getMessage(), "ab-notification-generic-trace");
    }

}
