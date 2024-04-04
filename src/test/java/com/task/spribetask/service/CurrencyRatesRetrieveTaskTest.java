package com.task.spribetask.service;

import com.task.spribetask.dto.CurrencyRates;
import com.task.spribetask.dto.external.APIResponse;
import com.task.spribetask.entity.CurrencyRatesEntity;
import com.task.spribetask.external.ExternalAPIClient;
import com.task.spribetask.repository.CurrencyRatesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyRatesRetrieveTaskTest {

    private static final String TEST_CURRENCY = "USD";
    private static final String TEST_BASE = "EUR";

    @Mock
    private ExternalAPIClient externalAPIClient;
    @Mock
    private ConcurrentHashMap<String, CurrencyRates> currencyRatesMap;
    @Mock
    private CurrencyRatesRepository currencyRatesRepository;
    @Mock
    private ConversionService conversionService;

    private CurrencyRatesRetrieveTask currencyRatesRetrieveTask;

    @BeforeEach
    void setUp() {
        currencyRatesRetrieveTask = new CurrencyRatesRetrieveTask(TEST_CURRENCY, currencyRatesMap,
                currencyRatesRepository, conversionService, externalAPIClient);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(currencyRatesMap, currencyRatesRepository, conversionService, externalAPIClient);
    }

    @Test
    void shouldRetrieveCurrencyRates() {
        var apiResponse = new APIResponse(true, 0, TEST_BASE, null, null, null);
        var currencyRatesEntity = new CurrencyRatesEntity();
        var currencyRates = new CurrencyRates(TEST_CURRENCY, null, null);

        when(externalAPIClient.getLatestRates()).thenReturn(java.util.Optional.of(apiResponse));
        when(conversionService.convert(apiResponse, CurrencyRatesEntity.class)).thenReturn(currencyRatesEntity);
        when(conversionService.convert(currencyRatesEntity, CurrencyRates.class)).thenReturn(currencyRates);

        currencyRatesRetrieveTask.run();

        verify(currencyRatesRepository).save(currencyRatesEntity);
        verify(currencyRatesMap).put(TEST_CURRENCY, currencyRates);
    }

    @Test
    void shouldNotRetrieveCurrencyRatesWhenAPIResponseWasNotSuccessful() {
        var apiResponse = new APIResponse(false, 0, TEST_BASE, null, null, null);

        when(externalAPIClient.getLatestRates()).thenReturn(java.util.Optional.of(apiResponse));

        currencyRatesRetrieveTask.run();
    }
}