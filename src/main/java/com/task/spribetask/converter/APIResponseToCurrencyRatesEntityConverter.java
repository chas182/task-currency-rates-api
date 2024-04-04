package com.task.spribetask.converter;

import com.task.spribetask.dto.external.APIResponse;
import com.task.spribetask.entity.CurrencyRatesEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.TimeZone;

@Component
public class APIResponseToCurrencyRatesEntityConverter implements Converter<APIResponse, CurrencyRatesEntity> {

    @Override
    public CurrencyRatesEntity convert(APIResponse source) {
        if (!source.isSuccess()) {
            return null;
        }
        var currencyRatesEntity = new CurrencyRatesEntity();

        currencyRatesEntity.setCurrency(source.getBase());

        var rateTimestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(source.getTimestamp()), TimeZone.getDefault().toZoneId());
        currencyRatesEntity.setDateTime(rateTimestamp);

        var newMap = new HashMap<String, BigDecimal>();
        source.getRates().forEach((currency, rate) -> newMap.put(currency, BigDecimal.valueOf(rate)));
        currencyRatesEntity.setRates(newMap);

        return currencyRatesEntity;
    }
}
