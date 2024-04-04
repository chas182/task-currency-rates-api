package com.task.spribetask.service;

import com.task.spribetask.dto.CurrencyRates;
import com.task.spribetask.dto.external.APIResponse;
import com.task.spribetask.entity.CurrencyRatesEntity;
import com.task.spribetask.external.ExternalAPIClient;
import com.task.spribetask.repository.CurrencyRatesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class CurrencyRatesRetrieveTask implements Runnable {

    private final String currency;
    private final ConcurrentHashMap<String, CurrencyRates> currencyRatesMap;
    private final CurrencyRatesRepository currencyRatesRepository;
    private final ConversionService conversionService;
    private final ExternalAPIClient externalAPIClient;

    @Override
    public void run() {
        log.info("Retrieve rates for '{}'", currency);

        externalAPIClient.getLatestRates()
                .filter(APIResponse::isSuccess)
                .ifPresentOrElse(apiResponse -> {
                    // Setting this because API free plan do not support 'base' parameter to get different currencies
                    apiResponse.setBase(currency);

                    var currencyRatesEntity = conversionService.convert(apiResponse, CurrencyRatesEntity.class);
                    var currencyRates = conversionService.convert(currencyRatesEntity, CurrencyRates.class);

                    currencyRatesRepository.save(currencyRatesEntity);
                    currencyRatesMap.put(currency, currencyRates);

                }, () -> log.error("Failed to retrieve currency rates for '{}'", currency));

    }
}