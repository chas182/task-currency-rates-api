create table if not exists currencies_rates
(
    id        serial primary key,
    currency  varchar(50),
    date_time timestamp,
    rates     jsonb
);

