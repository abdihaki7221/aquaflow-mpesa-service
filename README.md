# AquaFlow M-Pesa Integration Service

Safaricom Daraja API integration service for AquaFlow, built with **Spring Boot WebFlux** (reactive), **R2DBC + PostgreSQL**, and **Flyway** migrations.

## Features

| Feature | Endpoint | Description |
|---------|----------|-------------|
| **C2B Register URLs** | `POST /api/v1/mpesa/c2b/register-urls` | Register validation/confirmation callbacks with Safaricom |
| **C2B Validation** | `POST /api/v1/mpesa/c2b/validation` | Callback: validate incoming payment before processing |
| **C2B Confirmation** | `POST /api/v1/mpesa/c2b/confirmation` | Callback: confirm completed payment → triggers B2B |
| **B2B Auto-Disbursement** | *(automatic)* | On C2B confirmation, disburses 50% to another business |
| **B2B Result** | `POST /api/v1/mpesa/b2b/result` | Callback: B2B payment result from Safaricom |
| **B2B Timeout** | `POST /api/v1/mpesa/b2b/timeout` | Callback: B2B payment timeout |
| **Query by Trans ID** | `GET /api/v1/transactions/{transId}` | Fetch transaction by Safaricom reference ID |
| **Query by Account** | `GET /api/v1/transactions/account/{accountNumber}` | Fetch all transactions for an account number |
| **Query by Phone** | `GET /api/v1/transactions/phone/{msisdn}` | Fetch all transactions for a phone number |

## Architecture

```
Customer pays via M-Pesa (Paybill)
         │
         ▼
┌─────────────────────┐
│   Safaricom Daraja   │
│     C2B Gateway      │
└────────┬────────────┘
         │ POST /validation
         ▼
┌─────────────────────┐     ┌──────────────┐
│  AquaFlow Service    │────▶│  PostgreSQL   │
│  (Spring WebFlux)    │     │   (R2DBC)     │
└────────┬────────────┘     └──────────────┘
         │ POST /confirmation
         │
         │  ┌─── Save C2B transaction
         │  │
         │  └─── Auto B2B disbursement (50%)
         │              │
         ▼              ▼
┌─────────────────────┐
│   Safaricom Daraja   │
│   B2B Payment API    │
│                      │──▶ POST /b2b/result (callback)
└─────────────────────┘
```

## Prerequisites

- **Java 21+**
- **PostgreSQL 14+**
- **Maven 3.9+**
- **Safaricom Daraja Developer Account** → [developer.safaricom.co.ke](https://developer.safaricom.co.ke)
- **ngrok** (for local testing with Safaricom callbacks)

## Quick Start

### 1. Database Setup

```bash
createdb aquaflow_mpesa
```

### 2. Configure Daraja Credentials

Edit `src/main/resources/application.yml`:

```yaml
daraja:
  base-url: https://sandbox.safaricom.co.ke  # or https://api.safaricom.co.ke for production
  consumer-key: YOUR_CONSUMER_KEY
  consumer-secret: YOUR_CONSUMER_SECRET
  c2b:
    shortcode: "600000"
    confirmation-url: https://your-domain.com/api/v1/mpesa/c2b/confirmation
    validation-url: https://your-domain.com/api/v1/mpesa/c2b/validation
  b2b:
    initiator-name: testapi
    security-credential: YOUR_ENCRYPTED_CREDENTIAL
    sender-shortcode: "600000"
    receiver-shortcode: "600001"
    disbursement-percentage: 50
    queue-timeout-url: https://your-domain.com/api/v1/mpesa/b2b/timeout
    result-url: https://your-domain.com/api/v1/mpesa/b2b/result
```

### 3. Run

```bash
mvn spring-boot:run
```

### 4. Register C2B URLs (one-time)

```bash
curl -X POST http://localhost:8080/api/v1/mpesa/c2b/register-urls
```

### 5. Swagger UI

Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Local Testing with ngrok

```bash
ngrok http 8080
```

Update the callback URLs in `application.yml` with your ngrok URL:
```
https://abc123.ngrok.io/api/v1/mpesa/c2b/confirmation
```

Then re-register the URLs.

## API Examples

### Query transaction by reference
```bash
curl http://localhost:8080/api/v1/transactions/RKTQDM7W6S
```

### Query transactions by account number
```bash
curl http://localhost:8080/api/v1/transactions/account/ACC001
```

### Sample C2B Confirmation Response
```json
{
  "success": true,
  "message": "Transaction found",
  "data": {
    "id": 1,
    "transactionType": "Pay Bill",
    "transId": "RKTQDM7W6S",
    "transAmount": 1000.00,
    "billRefNumber": "ACC001",
    "msisdn": "254712345678",
    "customerName": "John Doe",
    "status": "CONFIRMED",
    "b2bDisbursed": true,
    "b2bTransaction": {
      "conversationId": "AG_20191219_000045...",
      "amount": 500.00,
      "status": "SUCCESS",
      "receiverShortcode": "600001"
    }
  },
  "timestamp": "2025-02-27T10:30:00"
}
```

## B2B Security Credential

For production, encrypt your initiator password using Safaricom's certificate:

1. Download the production certificate from Daraja portal
2. Use OpenSSL: `openssl rsautl -encrypt -in password.txt -out encrypted.txt -pubin -inkey cert.pem`
3. Base64-encode the output and set as `security-credential`

## Project Structure

```
src/main/java/com/aquaflow/
├── AquaFlowMpesaApplication.java
├── config/
│   ├── DarajaProperties.java         # Daraja config binding
│   ├── OpenApiConfig.java            # Swagger setup
│   └── WebClientConfig.java          # Reactive HTTP client
├── controller/
│   ├── B2BCallbackController.java    # B2B result/timeout callbacks
│   ├── C2BCallbackController.java    # C2B validation/confirmation + register
│   └── TransactionQueryController.java # Transaction query APIs
├── dto/
│   ├── daraja/                        # Daraja API request/response models
│   └── response/                      # Our API response models
├── entity/
│   ├── B2BTransaction.java
│   └── C2BTransaction.java
├── enums/
├── exception/
│   ├── DarajaApiException.java
│   ├── GlobalExceptionHandler.java
│   └── TransactionNotFoundException.java
├── repository/
│   ├── B2BTransactionRepository.java
│   └── C2BTransactionRepository.java
└── service/
    ├── B2BService.java                # B2B disbursement + result handling
    ├── C2BService.java                # C2B callback processing
    ├── DarajaAuthService.java         # OAuth token management
    └── TransactionQueryService.java   # Transaction queries
```
