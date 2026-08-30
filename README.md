# Customer Transaction API

A Spring Boot REST API for managing customer transactions. The application supports creating transactions, retrieving individual transactions, updating transaction status, and retrieving all transactions belonging to a customer.

## Technologies

* Java 17
* Spring Boot
* Spring Web
* Spring Data JPA
* H2 Database
* Maven
* JUnit 5
* MockMvc

## Project Structure

The application follows a simple layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
H2 Database
```

* **Controller** — Handles HTTP requests and responses.
* **Service** — Contains transaction business logic and validation rules.
* **Repository** — Provides database access using Spring Data JPA.
* **Entity** — Represents the transaction stored in the database.
* **DTO** — Represents API request data.
* **Exception Handler** — Provides consistent error responses.

## Transaction Model

Each transaction contains:

* Transaction ID
* Customer ID
* Amount
* Currency
* Transaction Type
* Transaction Status

Supported transaction statuses are:

```text
PENDING
COMPLETED
FAILED
```

Every newly created transaction is assigned the initial status `PENDING` by the service layer.

## Assumptions

The following assumptions were made while implementing the API:

* A newly created transaction always starts with `PENDING` status.
* `COMPLETED` and `FAILED` are terminal statuses and cannot be changed.
* Transaction IDs are unique.
* A customer can have multiple transactions.
* Retrieving transactions for a customer with no matching transactions returns an empty list with `200 OK`.
* H2 in-memory database is used for development and testing, so data is not persistent across application restarts.

## API Endpoints

### 1. Create Transaction

**POST** `/api/transactions`

Creates a new transaction.

Example request:

```json
{
  "transactionId": "TXN001",
  "customerId": "CUS001",
  "amount": 500.00,
  "currency": "INR",
  "transactionType": "PAYMENT"
}
```

A successful request returns `201 Created`.

The transaction status is automatically set to `PENDING`.

If the transaction ID already exists, the API returns `409 Conflict`.

### 2. Get Transaction

**GET** `/api/transactions/{transactionId}`

Retrieves a transaction using its transaction ID.

Example:

```text
GET /api/transactions/TXN001
```

A successful request returns `200 OK`.

If the transaction does not exist, the API returns `404 Not Found`.

### 3. Update Transaction Status

**PATCH** `/api/transactions/{transactionId}/status`

Updates the status of an existing transaction.

Example request:

```json
{
  "status": "COMPLETED"
}
```

The allowed status transitions are:

```text
PENDING → COMPLETED
PENDING → FAILED
```

`COMPLETED` and `FAILED` are treated as terminal states and cannot be changed.

This prevents an already finalized transaction from being modified and keeps the transaction lifecycle consistent.

If an attempt is made to update a transaction from a terminal state, the API returns `400 Bad Request`.

If the transaction does not exist, the API returns `404 Not Found`.

### 4. Get Customer Transactions

**GET** `/api/transactions/customer/{customerId}`

Retrieves all transactions associated with a customer.

Example:

```text
GET /api/transactions/customer/CUS001
```

A successful request returns `200 OK` with a list of transactions.

If the customer has no transactions, the API returns an empty list:

```json
[]
```

## Validation Rules

The following validation rules are applied to transaction creation requests:

* Transaction ID must not be blank.
* Customer ID must not be blank.
* Amount must not be null.
* Amount must be greater than zero.
* Currency must not be blank.
* Transaction Type must not be null.

The client does not provide the initial transaction status when creating a transaction. The service layer automatically sets it to `PENDING`.

### Business Validation

In addition to field-level validation, the service layer performs the following business validations:

**Duplicate transaction ID**

Before creating a transaction, the service checks whether the transaction ID already exists.

If it exists, the request is rejected with:

```text
409 Conflict
```

**Transaction status lifecycle**

Status changes are restricted to:

```text
PENDING → COMPLETED
PENDING → FAILED
```

Once a transaction reaches `COMPLETED` or `FAILED`, its status cannot be changed.

## Error Handling

A global exception handler provides consistent error responses.

The application handles:

| Situation                             |     HTTP Status |
| ------------------------------------- | --------------: |
| Invalid request data                  | 400 Bad Request |
| Invalid transaction status transition | 400 Bad Request |
| Duplicate transaction ID              |    409 Conflict |
| Transaction not found                 |   404 Not Found |

Validation errors include the fields that failed validation.

Example error response:

```json
{
  "status": 404,
  "timestamp": "2026-08-30T12:22:56.3038089",
  "message": "Transaction not found: TXN009",
  "error": "Not Found"
}
```

## Testing

The project uses JUnit 5, Spring Boot Test, and MockMvc for automated testing.

The test suite covers:

* Successful transaction creation
* Invalid transaction validation
* Duplicate transaction ID
* Non-existing transaction retrieval
* Successful transaction status update
* Invalid transaction status transition
* Non-existing transaction during status update
* Retrieving transactions for a customer
* Empty customer transaction results
* Spring application context loading

The complete test suite currently contains **10 tests**, and all tests pass successfully.

Run the tests with:

### Linux / macOS

```bash
./mvnw clean test
```

### Windows

```bat
mvnw.cmd clean test
```

## Database

The application uses an H2 in-memory database.

The database is intended for development and testing. Because it is an in-memory database, transaction data is cleared when the application is restarted.

## Known Limitations

* H2 is used as an in-memory database, so transaction data is not persistent across application restarts.
* There is no separate Customer entity; transactions are associated with customers using the `customerId` field.
* The application does not implement authentication or authorization.
* Pagination and sorting are not currently implemented for customer transaction retrieval.

## Improvements With More Time

The application could be extended with:

* A persistent production database such as PostgreSQL.
* Pagination and sorting for customer transaction retrieval.
* More detailed domain-specific validation.
* Authentication and authorization.
* API documentation using OpenAPI/Swagger.
* Additional service-layer unit tests.
* Improved transaction lifecycle handling as additional business requirements are introduced.
