package com.task.spribetask.exception;

import lombok.Getter;

@Getter
public class CurrencyServiceException extends RuntimeException {

    private final String error;
    private final String detailedMessage;

    public CurrencyServiceException(String error, String detailedMessage) {
        super("%s: %s.".formatted(error, detailedMessage));
        this.error = error;
        this.detailedMessage = detailedMessage;
    }
}
