-- =============================================================
-- V1__init_schema.sql
-- AquaFlow M-Pesa Integration - Initial Schema
-- =============================================================

-- Enum-like status tracking
CREATE TABLE IF NOT EXISTS mpesa_c2b_transactions (
    id                  BIGSERIAL PRIMARY KEY,
    transaction_type    VARCHAR(50)     NOT NULL,           -- e.g., Pay Bill
    trans_id            VARCHAR(50)     NOT NULL UNIQUE,    -- Safaricom transaction ID (e.g., RKTQDM7W6S)
    trans_time          VARCHAR(20)     NOT NULL,           -- e.g., 20191122063845
    trans_amount        DECIMAL(15, 2)  NOT NULL,
    business_shortcode  VARCHAR(20)     NOT NULL,
    bill_ref_number     VARCHAR(100),                       -- Account number the customer used
    invoice_number      VARCHAR(100),
    org_account_balance DECIMAL(15, 2),
    third_party_trans_id VARCHAR(100),
    msisdn              VARCHAR(20)     NOT NULL,           -- Customer phone number
    first_name          VARCHAR(100),
    middle_name         VARCHAR(100),
    last_name           VARCHAR(100),
    status              VARCHAR(30)     NOT NULL DEFAULT 'RECEIVED',  -- RECEIVED, VALIDATED, CONFIRMED, FAILED
    b2b_disbursed       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS mpesa_b2b_transactions (
    id                      BIGSERIAL PRIMARY KEY,
    c2b_transaction_id      BIGINT          REFERENCES mpesa_c2b_transactions(id),
    conversation_id         VARCHAR(100),           -- Daraja conversation ID
    originator_conversation_id VARCHAR(100),        -- Daraja originator conversation ID
    sender_shortcode        VARCHAR(20)     NOT NULL,
    receiver_shortcode      VARCHAR(20)     NOT NULL,
    amount                  DECIMAL(15, 2)  NOT NULL,
    command_id              VARCHAR(50)     NOT NULL,
    status                  VARCHAR(30)     NOT NULL DEFAULT 'INITIATED',  -- INITIATED, SUCCESS, FAILED, TIMEOUT
    result_type             INTEGER,
    result_code             INTEGER,
    result_desc             VARCHAR(500),
    transaction_id          VARCHAR(100),           -- Safaricom B2B transaction ID from result
    debit_account_balance   VARCHAR(200),
    credit_account_balance  VARCHAR(200),
    transaction_completed_time VARCHAR(50),
    raw_request             TEXT,
    raw_result              TEXT,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Indexes for query performance
CREATE INDEX idx_c2b_trans_id ON mpesa_c2b_transactions(trans_id);
CREATE INDEX idx_c2b_bill_ref ON mpesa_c2b_transactions(bill_ref_number);
CREATE INDEX idx_c2b_msisdn ON mpesa_c2b_transactions(msisdn);
CREATE INDEX idx_c2b_status ON mpesa_c2b_transactions(status);
CREATE INDEX idx_c2b_created_at ON mpesa_c2b_transactions(created_at);
CREATE INDEX idx_b2b_c2b_id ON mpesa_b2b_transactions(c2b_transaction_id);
CREATE INDEX idx_b2b_conversation_id ON mpesa_b2b_transactions(conversation_id);
CREATE INDEX idx_b2b_status ON mpesa_b2b_transactions(status);
