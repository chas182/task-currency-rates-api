package com.task.spribetask.converter;

import com.task.spribetask.dto.CurrencyRates;
import com.task.spribetask.entity.CurrencyRatesEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class CurrencyRatesEntityToCurrencyRatesConverter implements Converter<CurrencyRatesEntity, CurrencyRates> {
    @Override
    public CurrencyRates convert(CurrencyRatesEntity source) {
        var newMap = new HashMap<String, Double>();
        source.getRates().forEach((currency, rate) -> newMap.put(currency, rate.doubleValue()));
        return new CurrencyRates(source.getCurrency(), source.getDateTime(), newMap);
    }
}
