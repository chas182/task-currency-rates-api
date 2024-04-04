package com.task.spribetask.resource.exception;

import com.task.spribetask.exception.CurrencyServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class ResourceExceptionHandler {

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ApiError handleRuntimeException(RuntimeException e) {
        log.error("Unknown error.", e);
        return ApiError.of("Unknown error", e.getMessage());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(CurrencyServiceException.class)
    public ApiError handleCurrencyServiceException(CurrencyServiceException e) {
        log.error("Error: " + e.getMessage());
        return ApiError.of(e.getError(), e.getDetailedMessage());
    }
}
