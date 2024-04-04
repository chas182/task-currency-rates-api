package com.task.spribetask.service;

import com.task.spribetask.dto.CurrencyRates;
import com.task.spribetask.exception.CurrencyServiceException;
import com.task.spribetask.repository.CurrencyRatesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyService {

    private final ConcurrentHashMap<String, CurrencyRates> currencyRatesMap;
    private final CurrencyRatesRepository currencyRatesRepository;
    private final ConversionService conversionService;
    @Value("${currency.retrieve.interval}")
    private final Duration currencyRetrieveInterval;
    private final TaskScheduler taskScheduler;
    private final ObjectProvider<CurrencyRatesRetrieveTask> ratesRetrieveTaskObjectProvider;

    public List<String> getAllCurrencies() {
        return currencyRatesMap.keySet().stream().toList();
    }

    public CurrencyRates getCurrencyRate(String currency) {
        if (currencyRatesMap.containsKey(currency)) {
            return currencyRatesMap.get(currency);
        } else {
            throw new CurrencyServiceException("Currency not found", "Currency was not registered or we can't retrieve rates from external API");
        }
    }

    public void addCurrency(String currency) {
        if (currencyRatesMap.containsKey(currency)) {
            throw new CurrencyServiceException("Duplicated currency", "Currency was already registered");
        } else {
            taskScheduler.scheduleAtFixedRate(ratesRetrieveTaskObjectProvider.getObject(currency), currencyRetrieveInterval);
        }
    }

    /**
     * This method is used for filling up in memory Map with possible data from DB and setting up schedules for existing currency
     */
    public void init() {
        var latestRatesForAllCurrencies = currencyRatesRepository.findLatestRatesForAllCurrencies();
        if (!latestRatesForAllCurrencies.isEmpty()) {
            log.info("Found rates from DB: initializing map");
            latestRatesForAllCurrencies
                    .forEach(currencyRatesEntity -> {
                        currencyRatesMap.put(currencyRatesEntity.getCurrency(),
                                conversionService.convert(currencyRatesEntity, CurrencyRates.class));

                        taskScheduler.scheduleAtFixedRate(
                                ratesRetrieveTaskObjectProvider.getObject(currencyRatesEntity.getCurrency()),
                                // we need for example 1 hour interval for retrieving data,
                                // so here we're setting start time for job depending on date_time for last currency db record
                                currencyRatesEntity.getDateTime().plusSeconds(currencyRetrieveInterval.toSeconds()).atZone(ZoneId.systemDefault()).toInstant(),
                                currencyRetrieveInterval);
                    });
        }
    }

}
