package com.task.spribetask.converter;

import com.task.spribetask.dto.CurrencyRates;
import com.task.spribetask.dto.external.APIError;
import com.task.spribetask.dto.external.APIResponse;
import com.task.spribetask.entity.CurrencyRatesEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

class CurrencyRatesEntityToCurrencyRatesConverterTest {

    private static final String TEST_BASE = "USD";
    private static final long TEST_RATE_TIMESTAMP =  Instant.now().getEpochSecond();
    private static final LocalDateTime TEST_RATE_DATE_TIME = LocalDateTime.ofInstant(Instant.ofEpochSecond(TEST_RATE_TIMESTAMP), ZoneId.systemDefault());
    private static final Long TEST_ID = 1234L;
    private static final Map<String, Double> TEST_RATES = Map.of("GBP", 0.72007);
    private static final Map<String, BigDecimal> TEST_ENTITY_RATES = Map.of("GBP", new BigDecimal("0.72007"));

    private CurrencyRatesEntityToCurrencyRatesConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CurrencyRatesEntityToCurrencyRatesConverter();
    }

    @Test
    void shouldReturnExpectedCurrencyRatesOnConvert() {
        var apiResponse = buildCurrencyRatesEntity();
        var expectedCurrencyRates = buildCurrencyRates();

        var actualCurrencyRates = converter.convert(apiResponse);

        assertThat(actualCurrencyRates).isNotNull();
        assertThat(actualCurrencyRates).isEqualTo(expectedCurrencyRates);
    }

    @Test
    void shouldReturnExpectedCurrencyRatesOnConvertWhenRatesMapIsEmpty() {
        var apiResponse = buildCurrencyRatesEntityWithEmptyRatesMap();
        var expectedCurrencyRates = buildCurrencyRatesWithEmptyRatesMap();

        var actualCurrencyRates = converter.convert(apiResponse);

        assertThat(actualCurrencyRates).isNotNull();
        assertThat(actualCurrencyRates).isEqualTo(expectedCurrencyRates);
    }

    private static CurrencyRatesEntity buildCurrencyRatesEntity() {
        var currencyRatesEntity = new CurrencyRatesEntity();

        currencyRatesEntity.setId(TEST_ID);
        currencyRatesEntity.setCurrency(TEST_BASE);
        currencyRatesEntity.setDateTime(TEST_RATE_DATE_TIME);
        currencyRatesEntity.setRates(TEST_ENTITY_RATES);

        return currencyRatesEntity;
    }

    private static CurrencyRatesEntity buildCurrencyRatesEntityWithEmptyRatesMap() {
        var currencyRatesEntity = new CurrencyRatesEntity();

        currencyRatesEntity.setId(TEST_ID);
        currencyRatesEntity.setCurrency(TEST_BASE);
        currencyRatesEntity.setDateTime(TEST_RATE_DATE_TIME);
        currencyRatesEntity.setRates(Map.of());

        return currencyRatesEntity;
    }

    private static CurrencyRates buildCurrencyRates() {
        return new CurrencyRates(TEST_BASE, TEST_RATE_DATE_TIME, TEST_RATES);
    }

    private static CurrencyRates buildCurrencyRatesWithEmptyRatesMap() {
        return new CurrencyRates(TEST_BASE, TEST_RATE_DATE_TIME, Map.of());
    }

}