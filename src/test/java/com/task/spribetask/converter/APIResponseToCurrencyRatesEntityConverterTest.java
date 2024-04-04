package com.task.spribetask.converter;

import com.task.spribetask.dto.external.APIError;
import com.task.spribetask.dto.external.APIResponse;
import com.task.spribetask.entity.CurrencyRatesEntity;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
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

class APIResponseToCurrencyRatesEntityConverterTest {

    private static final String TEST_BASE = "USD";
    private static final long TEST_RATE_TIMESTAMP =  Instant.now().getEpochSecond();
    private static final LocalDateTime TEST_RATE_DATE_TIME = LocalDateTime.ofInstant(Instant.ofEpochSecond(TEST_RATE_TIMESTAMP), ZoneId.systemDefault());
    private static final LocalDate TEST_RATE_DATE = LocalDate.of(2024, 4, 2);
    private static final Map<String, Double> TEST_RATES = Map.of("GBP", 0.72007);
    private static final Map<String, BigDecimal> TEST_ENTITY_RATES = Map.of("GBP", new BigDecimal("0.72007"));

    private APIResponseToCurrencyRatesEntityConverter converter;

    @BeforeEach
    void setUp() {
        converter = new APIResponseToCurrencyRatesEntityConverter();
    }

    @Test
    void shouldReturnExpectedEntityOnConvert() {
        var apiResponse = buildApiResponse();

        var actualCurrencyRatesEntity = converter.convert(apiResponse);

        assertThat(actualCurrencyRatesEntity).isNotNull();
        assertThat(actualCurrencyRatesEntity.getId()).isNull();
        assertThat(actualCurrencyRatesEntity.getCurrency()).isEqualTo(TEST_BASE);
        assertThat(actualCurrencyRatesEntity.getDateTime()).isEqualTo(TEST_RATE_DATE_TIME);
        assertThat(actualCurrencyRatesEntity.getRates()).isEqualTo(TEST_ENTITY_RATES);
    }

    @Test
    void shouldReturnExpectedEntityOnConvertWhenRatesMapIsEmpty() {
        var apiResponse = buildApiResponseWithEmptyRatesMap();

        var actualCurrencyRatesEntity = converter.convert(apiResponse);

        assertThat(actualCurrencyRatesEntity).isNotNull();
        assertThat(actualCurrencyRatesEntity.getId()).isNull();
        assertThat(actualCurrencyRatesEntity.getCurrency()).isEqualTo(TEST_BASE);
        assertThat(actualCurrencyRatesEntity.getDateTime()).isEqualTo(TEST_RATE_DATE_TIME);
        assertThat(actualCurrencyRatesEntity.getRates()).isEmpty();
    }

    @Test
    void shouldReturnNullOnConvertWhenAPIResponseIsNotSuccess() {
        var apiResponse = buildApiResponseWithError();

        var actualCurrencyRatesEntity = converter.convert(apiResponse);

        assertThat(actualCurrencyRatesEntity).isNull();
    }

    private static APIResponse buildApiResponse() {
        return new APIResponse(true, TEST_RATE_TIMESTAMP, TEST_BASE, TEST_RATE_DATE, TEST_RATES, null);
    }

    private static APIResponse buildApiResponseWithEmptyRatesMap() {
        return new APIResponse(true, TEST_RATE_TIMESTAMP, TEST_BASE, TEST_RATE_DATE, Map.of(), null);
    }

    private static APIResponse buildApiResponseWithError() {
        return new APIResponse(false, 0, null, null, null, new APIError(101, EMPTY, EMPTY));
    }

}