/*
Tables created to store the exchange rates to be applied to transactions.

* Cons of the approach: Increased resource usage and the service assumes an external responsibility.

* Pros of the approach: Reduces dependency on an external system, avoiding issues due to service unavailability
and potential latency, while also including auditable records and historical access to the service.

*/

-- Creation of the RatesExchangeFilter table
CREATE TABLE purchase.exchange_rate_filter (
    id SERIAL PRIMARY KEY,
    effective_date_init DATE,
    effective_date_end DATE,
    currency VARCHAR(20),
    sort_order VARCHAR(4),
    page_size INTEGER,
    last_effective_date DATE,
    total_count INTEGER,
    total_pages INTEGER,
    status INTEGER,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_effective_date_init ON purchase.exchange_rate_filter (effective_date_init);
CREATE INDEX idx_effective_date_end ON purchase.exchange_rate_filter (effective_date_end);
CREATE INDEX idx_currency_exchange_rate_filter ON purchase.exchange_rate_filter (currency);

COMMENT ON TABLE purchase.exchange_rate_filter IS 'Table for storing rates exchange filter data.';

-- Comments for columns
COMMENT ON COLUMN purchase.exchange_rate_filter.id IS 'Unique identifier for each record.';
COMMENT ON COLUMN purchase.exchange_rate_filter.effective_date_init IS 'Effective date used on greater then or equal search..';
COMMENT ON COLUMN purchase.exchange_rate_filter.effective_date_end IS 'Effective date used on less then or equal search..';
COMMENT ON COLUMN purchase.exchange_rate_filter.currency IS 'Currency used on search.';
COMMENT ON COLUMN purchase.exchange_rate_filter.sort_order IS 'Sort order (ASC or DESC) used on search.';
COMMENT ON COLUMN purchase.exchange_rate_filter.page_size IS 'Page size used on search.';
COMMENT ON COLUMN purchase.exchange_rate_filter.last_effective_date IS 'Most recent effective date returned by the service.';
COMMENT ON COLUMN purchase.exchange_rate_filter.total_count IS 'Total number of records returned by external service.';
COMMENT ON COLUMN purchase.exchange_rate_filter.total_pages IS 'Total number of pages returned by external service.';
COMMENT ON COLUMN purchase.exchange_rate_filter.status IS 'Status of processing (1 = Created, 2 = In progress, 3 = Completed, 4 = Processing with errors).';
COMMENT ON COLUMN purchase.exchange_rate_filter.date_creation IS 'Creation date of the record.';

GRANT SELECT, INSERT, UPDATE, DELETE ON purchase.exchange_rate_filter TO pts_sys_user;

CREATE TABLE purchase.exchange_rate (
    id SERIAL,  -- Unique identifier for each record
    currency VARCHAR(20),  -- Currency code (ISO 4217)
    effective_date DATE,  -- Effective date of the exchange rate
    exchange_rate DECIMAL(18, 6),  -- Exchange rate
    search_filter_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rates_exchange_filter
        FOREIGN KEY (search_filter_id)
        REFERENCES purchase.exchange_rate_filter(id),
    CONSTRAINT pk_exchange_rate PRIMARY KEY (id, effective_date)
) PARTITION BY RANGE (effective_date);

CREATE INDEX idx_currency ON purchase.exchange_rate (currency);

-- Comments
COMMENT ON TABLE purchase.exchange_rate IS 'Table for storing purchase exchange rate data.';
COMMENT ON COLUMN purchase.exchange_rate.id IS 'Unique identifier for each record.';
COMMENT ON COLUMN purchase.exchange_rate.currency IS 'Currency code (ISO 4217).';
COMMENT ON COLUMN purchase.exchange_rate.effective_date IS 'Effective date of the exchange rate.';
COMMENT ON COLUMN purchase.exchange_rate.exchange_rate IS 'Exchange rate.';
COMMENT ON COLUMN purchase.exchange_rate.search_filter_id IS 'Foreign key to exchange_rate_filter table. Identifies the search filter associated with the exchange rate.';

GRANT SELECT, INSERT, UPDATE, DELETE ON purchase.exchange_rate TO pts_sys_user;
