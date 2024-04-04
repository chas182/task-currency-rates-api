package com.task.spribetask.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "currencies_rates")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CurrencyRatesEntity {

    private static final String CURRENCIES_RATES_ID_SEQ = "currencies_rates_id_seq";

    @Id
    @SequenceGenerator(name = CURRENCIES_RATES_ID_SEQ, sequenceName = CURRENCIES_RATES_ID_SEQ, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = CURRENCIES_RATES_ID_SEQ)
    @Column(name = "id")
    private Long id;
    @Column(name = "currency")
    private String currency;
    @Column(name = "date_time")
    private LocalDateTime dateTime;
    @Column(name = "rates")
    @Type(JsonType.class)
    private Map<String, BigDecimal> rates;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CurrencyRatesEntity that = (CurrencyRatesEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
