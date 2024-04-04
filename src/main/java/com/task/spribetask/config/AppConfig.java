package com.task.spribetask.config;

import com.task.spribetask.dto.CurrencyRates;
import com.task.spribetask.external.ExternalAPIClient;
import com.task.spribetask.repository.CurrencyRatesRepository;
import com.task.spribetask.service.CurrencyRatesRetrieveTask;
import com.task.spribetask.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final CurrencyRatesRepository currencyRatesRepository;
    private final ConversionService conversionService;
    @Value("${currency.retrieve.interval}")
    private final Duration currencyRetrieveInterval;
    private final ObjectProvider<CurrencyRatesRetrieveTask> ratesRetrieveTaskObjectProvider;
    private final ExternalAPIClient externalAPIClient;

    @Bean
    public ConcurrentHashMap<String, CurrencyRates> currencyRatesMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean(initMethod = "init")
    public CurrencyService currencyService() {
        return new CurrencyService(currencyRatesMap(), currencyRatesRepository, conversionService,
                currencyRetrieveInterval, taskScheduler(), ratesRetrieveTaskObjectProvider);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        var threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }

    @Bean
    @Scope("prototype")
    public CurrencyRatesRetrieveTask currencyRatesRetrieveTask(String currency) {
        return new CurrencyRatesRetrieveTask(currency, currencyRatesMap(), currencyRatesRepository, conversionService,
                externalAPIClient);
    }

}
