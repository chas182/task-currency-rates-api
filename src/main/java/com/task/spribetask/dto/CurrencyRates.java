package com.task.spribetask.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record CurrencyRates(String currency,
                            LocalDateTime dateTime,
                            Map<String, Double> rates) {
}
