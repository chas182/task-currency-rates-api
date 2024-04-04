package com.task.spribetask.resource.exception;

import java.time.LocalDateTime;

public record ApiError(String message,
                       String detailedMessage,
                       LocalDateTime timestamp) {

    public static ApiError of(String message, String detailedMessage) {
        return new ApiError(message, detailedMessage, LocalDateTime.now());
    }
}
