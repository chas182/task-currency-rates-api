create table if not exists currencies_rates
(
    id        serial primary key,
    currency  varchar(50),
    date_time timestamp,
    rates     jsonb
);


insert into currencies_rates (currency, date_time, rates)
values
    ('EUR', '2024-04-02 00:00:00', '{"GBP": 0.72007, "JPY": 107.346001}'),
    ('USD', '2024-04-02 00:00:00', '{"GBP": 0.72007, "JPY": 107.346001}');