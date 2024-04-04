package com.task.spribetask.external;

import com.task.spribetask.dto.external.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalAPIClient {

    // Not using 'base' parameter to get different currencies API free plan do not support it.
    private static final String PATH_GET_LATEST_RATES = "/latest?access_key={accessKey}";

    @Value("${currency.service.access.key}")
    private final String accessKey;
    @Qualifier("currencyRestTemplate")
    private final RestTemplate restTemplate;

    public Optional<APIResponse> getLatestRates() {
        try {
            log.info("Calling '/latest'");
            var apiResponseOptional = Optional.ofNullable(restTemplate.getForObject(PATH_GET_LATEST_RATES, APIResponse.class, accessKey));
            apiResponseOptional
                    .filter(apiResponse -> !apiResponse.isSuccess())
                    .ifPresent(apiResponse -> log.error("Error: " + apiResponse.getError()));
            return apiResponseOptional;
        } catch (HttpStatusCodeException e) {
            log.error("Error while calling '/latest': {}", e.getMessage());
            return Optional.empty();
        }
    }
}
