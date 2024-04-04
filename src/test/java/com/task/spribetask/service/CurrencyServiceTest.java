package com.task.spribetask.service;

import com.task.spribetask.dto.CurrencyRates;
import com.task.spribetask.entity.CurrencyRatesEntity;
import com.task.spribetask.exception.CurrencyServiceException;
import com.task.spribetask.repository.CurrencyRatesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.TaskScheduler;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    private static final Duration TEST_DURATION = Duration.ofMinutes(50);
    private static final String TEST_CURRENCY_USD = "USD";
    private static final String TEST_CURRENCY_EUR = "EUR";
    private static final LocalDate TEST_RATE_DATE = LocalDate.of(2024, 4, 2);
    private static final LocalDateTime TEST_RATE_DATE_TIME = LocalDateTime.of(TEST_RATE_DATE, LocalTime.MAX);
    private static final Map<String, Double> TEST_RATES = Map.of("GBP", 0.72007, "JPY", 107.346001, "EUR", 0.813399, "BTC", 1.6295132e-5);

    @Mock
    private CurrencyRatesRetrieveTask ratesRetrieveTask1;
    @Mock
    private CurrencyRatesRetrieveTask ratesRetrieveTask2;
    @Mock
    private CurrencyRatesRepository currencyRatesRepository;
    @Mock
    private ConversionService conversionService;
    @Mock
    private TaskScheduler taskScheduler;
    @Mock
    private ObjectProvider<CurrencyRatesRetrieveTask> ratesRetrieveTaskObjectProvider;
    private ConcurrentHashMap<String, CurrencyRates> currencyRatesMap;

    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        currencyRatesMap = new ConcurrentHashMap<>();
        currencyService = new CurrencyService(currencyRatesMap, currencyRatesRepository, conversionService,
                TEST_DURATION, taskScheduler, ratesRetrieveTaskObjectProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(currencyRatesRepository, conversionService, taskScheduler, ratesRetrieveTaskObjectProvider);
    }

    @Test
    public void shouldGetAllCurrencies() {
        List<String> expectedCurrencies = List.of(TEST_CURRENCY_USD, TEST_CURRENCY_EUR);
        currencyRatesMap.put(TEST_CURRENCY_USD, new CurrencyRates(TEST_CURRENCY_USD, TEST_RATE_DATE_TIME, TEST_RATES));
        currencyRatesMap.put(TEST_CURRENCY_EUR, new CurrencyRates(TEST_CURRENCY_EUR, TEST_RATE_DATE_TIME, TEST_RATES));

        List<String> actualCurrencies = currencyService.getAllCurrencies();

        assertThat(actualCurrencies).hasSameElementsAs(expectedCurrencies);
    }

    @Test
    public void shouldGetCurrencyRateWhenCurrencyIsExist() {
        var currencyRates = new CurrencyRates(TEST_CURRENCY_USD, TEST_RATE_DATE_TIME, TEST_RATES);
        currencyRatesMap.put(TEST_CURRENCY_USD, currencyRates);

        CurrencyRates actualRates = currencyService.getCurrencyRate(TEST_CURRENCY_USD);

        assertThat(actualRates).isEqualTo(currencyRates);
    }

    @Test
    public void shouldThrowAnExceptionOnGetCurrencyRateWhenCurrencyIsNotExist() {
        var currencyRates = new CurrencyRates(TEST_CURRENCY_USD, TEST_RATE_DATE_TIME, TEST_RATES);
        currencyRatesMap.put(TEST_CURRENCY_USD, currencyRates);

        assertThrows(CurrencyServiceException.class, () -> currencyService.getCurrencyRate("NotValid"));
    }

    @Test
    public void shouldAddCurrency() {
        when(ratesRetrieveTaskObjectProvider.getObject(TEST_CURRENCY_USD)).thenReturn(ratesRetrieveTask1);

        currencyService.addCurrency(TEST_CURRENCY_USD);

        verify(taskScheduler).scheduleAtFixedRate(ratesRetrieveTask1, TEST_DURATION);
    }

    @Test
    public void shouldThrowAnExceptionOnAddCurrencyWhenCurrencyIsNotExist() {
        var currencyRates = new CurrencyRates(TEST_CURRENCY_USD, TEST_RATE_DATE_TIME, TEST_RATES);
        currencyRatesMap.put(TEST_CURRENCY_USD, currencyRates);

        assertThrows(CurrencyServiceException.class, () -> currencyService.addCurrency(TEST_CURRENCY_USD));
    }

    @Test
    public void shouldInitWhenNoRatesInDB() {
        when(currencyRatesRepository.findLatestRatesForAllCurrencies()).thenReturn(List.of());

        currencyService.init();

        assertThat(currencyRatesMap).isEmpty();
        verify(taskScheduler, never()).scheduleAtFixedRate(any(), any());
    }

    @Test
    public void shouldInitWhenThereAreRatesInDB() {
        var now = LocalDateTime.now();
        var localDateTime1 = now.minusMinutes(60);
        var localDateTime2 = now.minusMinutes(30);

        var scheduledJobDateTime1 = localDateTime1.plusMinutes(TEST_DURATION.toMinutes()).atZone(ZoneId.systemDefault()).toInstant();
        var scheduledJobDateTime2 = localDateTime2.plusMinutes(TEST_DURATION.toMinutes()).atZone(ZoneId.systemDefault()).toInstant();

        var entity1 = new CurrencyRatesEntity();
        entity1.setCurrency(TEST_CURRENCY_USD);
        entity1.setDateTime(localDateTime1);

        var entity2 = new CurrencyRatesEntity();
        entity2.setCurrency(TEST_CURRENCY_EUR);
        entity2.setDateTime(now.minusMinutes(30));

        var currencyRate1 = new CurrencyRates(TEST_CURRENCY_USD, localDateTime1, TEST_RATES);
        var currencyRate2 = new CurrencyRates(TEST_CURRENCY_EUR, localDateTime2, TEST_RATES);

        when(conversionService.convert(entity1, CurrencyRates.class)).thenReturn(currencyRate1);
        when(conversionService.convert(entity2, CurrencyRates.class)).thenReturn(currencyRate2);
        when(ratesRetrieveTaskObjectProvider.getObject(TEST_CURRENCY_USD)).thenReturn(ratesRetrieveTask1);
        when(ratesRetrieveTaskObjectProvider.getObject(TEST_CURRENCY_EUR)).thenReturn(ratesRetrieveTask2);
        when(currencyRatesRepository.findLatestRatesForAllCurrencies()).thenReturn(List.of(entity1, entity2));

        currencyService.init();

        assertThat(currencyRatesMap)
                .containsAllEntriesOf(Map.of(TEST_CURRENCY_USD, currencyRate1, TEST_CURRENCY_EUR, currencyRate2));
        verify(taskScheduler).scheduleAtFixedRate(ratesRetrieveTask1, scheduledJobDateTime1, TEST_DURATION);
        verify(taskScheduler).scheduleAtFixedRate(ratesRetrieveTask2, scheduledJobDateTime2, TEST_DURATION);
    }
}