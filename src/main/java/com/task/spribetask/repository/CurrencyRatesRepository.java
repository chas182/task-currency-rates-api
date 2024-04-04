package com.task.spribetask.repository;

import com.task.spribetask.entity.CurrencyRatesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRatesRepository extends JpaRepository<CurrencyRatesEntity, Long> {

    @Query(value = "select distinct on (currency) * from currencies_rates order by currency, date_time desc",
            nativeQuery = true)
    List<CurrencyRatesEntity> findLatestRatesForAllCurrencies();

    @Query("SELECT DISTINCT cr.currency FROM CurrencyRatesEntity cr")
    List<String> findDistinctCurrencies();

    Optional<CurrencyRatesEntity> findFirstByCurrencyOrderByDateTimeDesc(String currency);
}
