# Customer Transactions Service

Submission for the Toucan Payments Engineering Challenge 2026.
Built inside the provided Spring Boot starter (Java 17, Spring Boot 3.5.5, Maven, H2, JUnit).

## Problem understanding

A small REST service that manages customer transactions: create a transaction,
fetch one by ID, move it through a defined status lifecycle, and list all
transactions for a customer. The emphasis is on correct validation, deliberate
error handling and meaningful tests rather than feature count.

## Assumptions

My invitation email did not contain a candidate-specific variant, so per the
challenge instruction that candidates define their own validation rules, I
defined and documented the following:

- Supported currencies: **INR, USD, EUR**
- Transaction types: **PAYMENT, REFUND, TRANSFER**
- Amount: **greater than 0, at most 100,000** (inclusive), interpreted as the
  same numeric cap regardless of currency (a simplification)
- Transaction IDs are client-supplied and globally unique
- Every new transaction starts as **PENDING**; clients cannot choose the initial status
- Customer IDs are opaque strings — this service does not own customer records,
  so it cannot verify that a customer "exists"

## API endpoints

| Operation | Endpoint | Success | Errors |
|---|---|---|---|
| Create transaction | `POST /api/transactions` | 201 | 400 validation, 409 duplicate ID |
| Get transaction | `GET /api/transactions/{transactionId}` | 200 | 404 not found |
| Update status | `PATCH /api/transactions/{transactionId}/status` | 200 | 400 bad status, 404, 409 illegal transition |
| Customer transactions | `GET /api/customers/{customerId}/transactions` | 200 (empty list if none) | — |

Create request body:

```json
{
  "transactionId": "TXN-1",
  "customerId": "CUST-1",
  "amount": 250.50,
  "currency": "INR",
  "type": "PAYMENT"
}
```

Update status body: `{ "status": "COMPLETED" }`

All errors share one shape:

```json
{ "timestamp": "...", "status": 409, "message": "Transaction 'TXN-1' already exists" }
```

Validation failures additionally carry a `fieldErrors` map keyed by field name.

## Validation rules

- `transactionId`, `customerId`: must not be blank (Bean Validation, 400)
- `amount`: required, > 0, ≤ 100,000 (Bean Validation, 400)
- `currency`, `type`, `status`: must be one of the enum values — unknown values
  are rejected during JSON parsing with a message listing the allowed values (400)
- Duplicate `transactionId`: rejected in the service with 409; the database
  primary key enforces the same rule as a second line of defence
- Amounts are `BigDecimal` end to end; `double` is never used for money

## Status transition rules

| Current | Allowed next | Reasoning |
|---|---|---|
| PENDING | COMPLETED | Processing succeeded |
| PENDING | FAILED | Processing failed or was rejected |
| COMPLETED | — | Terminal: a settled transaction is an immutable record; a reversal is a new REFUND transaction |
| FAILED | — | Terminal: failed attempts stay on record; a retry is a new transaction |

Self-transitions and anything → PENDING are rejected (409). The rules live in
`TransactionStatus.canTransitionTo` — one method, no workflow engine.

## Architecture

```
TransactionController  →  TransactionService  →  TransactionRepository  →  H2
        DTOs                business rules          Spring Data JPA
```

Request/response DTOs keep the API contract separate from the JPA entity (and
make "clients cannot set status" structural). A `@RestControllerAdvice` maps
the three business exceptions and validation failures to consistent JSON errors.

## Testing approach

15 full-stack MockMvc tests (`TransactionApiTests`) run every request through
the real controller, service, repository and H2, asserting on status codes and
response bodies. They cover: successful create, each validation rule, duplicate
ID, get found/not-found, legal and illegal status transitions (including that
changes persist), unknown status value, and customer lookup with and without
transactions. `@Transactional` rolls the database back between tests.

## How to run

```
mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run        # Linux/macOS
```

The API is then on `http://localhost:8080`. Run tests with:

```
mvnw.cmd clean test
```

## Known limitations

- H2 is in-memory with `ddl-auto: create-drop`: data is lost on restart
- The duplicate check has a theoretical race between check and insert; the
  primary key still prevents a duplicate row, but that path would surface as a
  500 rather than a clean 409
- The amount cap does not account for currency (100,000 EUR ≠ 100,000 INR)
- No pagination on the customer transaction list
- No authentication — out of scope per the challenge

## With more time

- A `CANCELLED` status for user-initiated abandonment before processing
- Catch the database constraint violation to return 409 even in the race case
- Per-currency amount limits and pagination
- An OpenAPI spec for the four endpoints
