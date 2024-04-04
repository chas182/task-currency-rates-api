package com.task.spribetask.resource;

import com.task.spribetask.dto.CurrencyRates;
import com.task.spribetask.exception.CurrencyServiceException;
import com.task.spribetask.service.CurrencyService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyResource.class)
class CurrencyResourceTest {

    private static final String GET_CURRENCIES_LIST = "/currency/list";
    private static final String GET_LATEST_CURRENCY_RATES = "/currency/latest";
    private static final String POST_CURRENCY = "/currency/add";

    private static final String TEST_CURRENCY = "USD";
    private static final LocalDateTime TEST_RATE_DATE_TIME = LocalDate.now().atStartOfDay();
    private static final Map<String, Double> TEST_RATES = Map.of("EUR", 0.72007);

    private static final String JSON_200_CURRENCIES_LIST = "payload/resource/200_currencies_list.json";
    private static final String JSON_200_CURRENCY_RATES = "payload/resource/200_currency_rates.json";


    @MockBean
    private CurrencyService currencyService;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(currencyService);
    }

    @Test
    @SneakyThrows
    void shouldGetCurrenciesList() {
        when(currencyService.getAllCurrencies()).thenReturn(List.of(TEST_CURRENCY));

        mockMvc.perform(get(GET_CURRENCIES_LIST))
                .andExpect(status().isOk())
                .andExpect(content().json(getJson200CurrenciesList()));

        verify(currencyService).getAllCurrencies();
    }

    @Test
    @SneakyThrows
    void shouldGetLatestRate() {
        when(currencyService.getCurrencyRate(TEST_CURRENCY)).thenReturn(buildCurrencyRates());

        mockMvc.perform(get(GET_LATEST_CURRENCY_RATES)
                        .queryParam("currency", TEST_CURRENCY))
                .andExpect(status().isOk())
                .andExpect(content().json(getJson200CurrencyRates()
                        .formatted(TEST_RATE_DATE_TIME.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(currencyService).getCurrencyRate(TEST_CURRENCY);
    }

    @Test
    @SneakyThrows
    void shouldReceiveAPIErrorOnGetLatestRateWhenCurrencyIsNotExist() {
        when(currencyService.getCurrencyRate(TEST_CURRENCY)).thenThrow(new CurrencyServiceException("Boo", "Booo!!!"));

        mockMvc.perform(get(GET_LATEST_CURRENCY_RATES)
                        .queryParam("currency", TEST_CURRENCY))
                .andExpect(status().isBadRequest());

        verify(currencyService).getCurrencyRate(TEST_CURRENCY);
    }

    @Test
    @SneakyThrows
    void shouldReceiveAPIErrorOnGetLatestRateWhenRuntimeExceptionOccurred() {
        when(currencyService.getCurrencyRate(TEST_CURRENCY)).thenThrow(new RuntimeException("Boo"));

        mockMvc.perform(get(GET_LATEST_CURRENCY_RATES)
                        .queryParam("currency", TEST_CURRENCY))
                .andExpect(status().isInternalServerError());

        verify(currencyService).getCurrencyRate(TEST_CURRENCY);
    }

    @Test
    @SneakyThrows
    void shouldAddCurrency() {
        mockMvc.perform(post(POST_CURRENCY)
                        .queryParam("currency", TEST_CURRENCY))
                .andExpect(status().isOk());

        verify(currencyService).addCurrency(TEST_CURRENCY);
    }

    @Test
    @SneakyThrows
    void shouldReceiveAPIErrorOnAddCurrencyWhenCurrencyIsAlreadyExist() {
        doThrow(new CurrencyServiceException("Boo", "Booo!!!")).when(currencyService).addCurrency(TEST_CURRENCY);

        mockMvc.perform(post(POST_CURRENCY)
                        .queryParam("currency", TEST_CURRENCY))
                .andExpect(status().isBadRequest());

        verify(currencyService).addCurrency(TEST_CURRENCY);
    }

    @Test
    @SneakyThrows
    void shouldReceiveAPIErrorOnAddCurrencyWhenRuntimeExceptionOccurred() {
        doThrow(new RuntimeException("Boo")).when(currencyService).addCurrency(TEST_CURRENCY);

        mockMvc.perform(post(POST_CURRENCY)
                        .queryParam("currency", TEST_CURRENCY))
                .andExpect(status().isInternalServerError());

        verify(currencyService).addCurrency(TEST_CURRENCY);
    }

    private static CurrencyRates buildCurrencyRates() {
        return new CurrencyRates(TEST_CURRENCY, TEST_RATE_DATE_TIME, TEST_RATES);
    }

    @SneakyThrows
    private static String getJson200CurrenciesList() {
        return Files.readString(Paths.get("src/test/resources", JSON_200_CURRENCIES_LIST));
    }

    @SneakyThrows
    private static String getJson200CurrencyRates() {
        return Files.readString(Paths.get("src/test/resources", JSON_200_CURRENCY_RATES));
    }

}