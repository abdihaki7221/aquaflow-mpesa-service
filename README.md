# AquaFlow M-Pesa Integration Service

Spring Boot WebFlux backend for AquaFlow water billing with Safaricom Daraja M-Pesa integration.

## API Endpoints

### Meters & Billing
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/meters` | Get all meters |
| GET | `/api/v1/meters/{meterNumber}` | Get meter by number |
| POST | `/api/v1/meters/reading` | Record new meter reading |
| POST | `/api/v1/meters/calculate-bill` | Calculate bill for meter + reading |
| POST | `/api/v1/meters/{meterNumber}/generate-bill` | Generate water bill |
| GET | `/api/v1/meters/{meterNumber}/bills` | Get bills for meter |
| GET | `/api/v1/meters/bills/unpaid` | Get all unpaid bills |

### M-Pesa STK Push
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/mpesa/stk/push` | Send STK push to customer phone |
| POST | `/api/v1/mpesa/stk/callback` | STK callback (from Safaricom) |
| GET | `/api/v1/mpesa/stk/status/{checkoutRequestId}` | Check payment status |
| GET | `/api/v1/mpesa/stk/meter/{meterNumber}` | Get STK history for meter |

### M-Pesa C2B
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/mpesa/c2b/register-urls` | Register callback URLs |
| POST | `/api/v1/mpesa/c2b/validation` | Validation callback |
| POST | `/api/v1/mpesa/c2b/confirmation` | Confirmation callback |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/transactions/{transId}` | Get by M-Pesa reference |
| GET | `/api/v1/transactions/account/{accountNumber}` | Get by account/meter |
| GET | `/api/v1/transactions/phone/{msisdn}` | Get by phone number |

### Onboarding
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/onboard` | Submit self-onboarding application |

## Quick Start

### 1. Database
```bash
createdb aquaflow_mpesa
```

### 2. Configure
Edit `src/main/resources/application.yml` or set environment variables:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`
- `DARAJA_CONSUMER_KEY`, `DARAJA_CONSUMER_SECRET`
- `STK_SHORTCODE`, `STK_PASSKEY`, `STK_CALLBACK_URL`

### 3. Run
```bash
mvn spring-boot:run
```

### 4. Swagger UI
http://localhost:8080/swagger-ui.html

## Tech Stack
- Java 21, Spring Boot 3.3, WebFlux (reactive)
- R2DBC + PostgreSQL, Flyway migrations
- Safaricom Daraja API (C2B, B2B, STK Push)
- SpringDoc OpenAPI (Swagger)
