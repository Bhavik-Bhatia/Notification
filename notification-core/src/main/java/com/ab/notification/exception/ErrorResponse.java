package com.ab.notification.exception;

import java.io.Serializable;

public class ErrorResponse implements Serializable {

    public static final long serialVersionUID = 1L;

    private final String errorCode;
    private final String errorMessage;
    private final String traceId;

    public ErrorResponse(String errorCode, String errorMessage,String traceId) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.traceId = traceId;
    }

    @Override
    public String toString() {
        return "ErrorResponse{errorCode=" + errorCode + ", errorMessage=" + errorMessage + ", traceId=" + traceId + "}";
    }

}
