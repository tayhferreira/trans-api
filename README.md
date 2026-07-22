# Purchase Transaction API

Spring Boot microservice to:

- store purchase transactions in USD;
- retrieve transactions by ID;
- convert transaction amounts to another currency using Treasury API rates.

## Stack

- Java 25
- Spring Boot 4
- Spring Data JDBC
- H2 Database (in-memory)
- SpringDoc OpenAPI (Swagger UI)
- Gradle Wrapper

## Environment Requirements

- JDK 25 installed and configured in `JAVA_HOME`
- Internet access to query the Treasury API (currency conversion)
- Port `8080` available (or configure a different port)

## How to Run

```bash
./gradlew clean bootRun
```

Application starts at:

- `http://localhost:8080`

### Run on a Different Port

```bash
./gradlew bootRun --args="--server.port=8085"
```

## How to Run Tests

```bash
./gradlew clean test
```

Full build:

```bash
./gradlew clean build
```

## API Documentation

With the application running:

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Postman Collection

To make API validation easier, version the Postman assets inside the repository:

- Collection: `postman/collection/purchase-transaction-api.postman_collection.json`
- Environment template (optional): `postman/environment/local.postman_environment.template.json`

How to import:

1. Open Postman and click **Import**.
2. Select the JSON file from the `postman/collection` folder.
3. (Optional) Import the environment template and set `baseUrl` (for example, `http://localhost:8080`).

## Exposed Endpoints

Base URL: `http://localhost:8080`

- `POST /api/transactions`  
  Creates a new purchase transaction.

- `GET /api/transactions/{id}`  
  Retrieves a transaction by ID.

- `GET /api/transactions/{id}/convert?currency=EUR`  
  Retrieves the transaction and converts its amount to the target currency.

## Currency Conversion Rule (Product Brief)

For `GET /api/transactions/{id}/convert`:

- The exchange rate must have a `record_date` less than or equal to the purchase `transaction_date`.
- The exchange rate must be within the previous 6 months from the purchase date.
- If multiple rates match, the API uses the most recent valid one.
- If no valid rate exists in this 6-month window, the API returns `EXCHANGE_RATE_NOT_FOUND`.
- The converted amount is rounded to 2 decimal places.

## Supported Currencies (Current Mapping)

The application uses explicit currency-name mapping for Treasury API filters via configuration in `application.properties` (`wex.treasury.currency-name-mapping.*`):

- `BRL` -> `Real`
- `EUR` -> `Euro`
- `JPY` -> `Yen`
- `GBP` -> `Pound`
- `CAD` -> `Dollar`
- `AUD` -> `Dollar`
- `CHF` -> `Franc`

For other 3-letter ISO currency codes, the application attempts a fallback using Java `Currency` display names in English.  
If the Treasury API does not support that name/code combination, conversion may return `EXCHANGE_RATE_NOT_FOUND`.

## Quick Example (curl)

### 1) Create transaction

```bash
curl -s -X POST "http://localhost:8080/api/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "description":"Office Supplies",
    "transactionDate":"2024-08-15",
    "amount":100.00
  }'
```

### 2) Get by ID

```bash
curl -i "http://localhost:8080/api/transactions/{id}"
```

### 3) Convert currency

```bash
curl -i "http://localhost:8080/api/transactions/{id}/convert?currency=EUR"
```

## Important Notes

- H2 is configured in-memory (`jdbc:h2:mem`), so data is lost when the application restarts.
- If the conversion endpoint returns an exchange-rate error, verify Treasury API connectivity and data availability for the requested currency/date.
