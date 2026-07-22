
CREATE TABLE IF NOT EXISTS "purchases" (
    "id" VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    "description" VARCHAR(50) NOT NULL,
    "transaction_date" TIMESTAMP NOT NULL,
    "amount_usd" DECIMAL(12, 2) NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "exchange_rates" (
    "id" BIGINT AUTO_INCREMENT PRIMARY KEY,
    "currency" VARCHAR(3) NOT NULL,
    "quarter" INT NOT NULL,
    "rate_year" INT NOT NULL,
    "rate_date" DATE NOT NULL,
    "rate" DECIMAL(10, 6) NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "unique_rate" UNIQUE ("currency", "rate_date")
);

CREATE INDEX "idx_purchases_transaction_date" ON "purchases"("transaction_date");
CREATE INDEX "idx_exchange_rates_currency_rate_date" ON "exchange_rates"("currency", "rate_date");
