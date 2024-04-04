package com.task.spribetask.dto.external;

public record APIError(int code,
                       String type,
                       String info) {
}
