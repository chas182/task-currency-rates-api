package com.task.spribetask.dto.external;

import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public final class APIResponse {
    private boolean success;
    private long timestamp;
    @Setter
    private String base;
    private LocalDate date;
    private Map<String, Double> rates;
    private APIError error;
}


