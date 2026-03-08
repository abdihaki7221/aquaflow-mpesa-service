-- Water meters
CREATE TABLE IF NOT EXISTS meters (
    id              BIGSERIAL PRIMARY KEY,
    meter_number    VARCHAR(20)  NOT NULL UNIQUE,
    tenant_name     VARCHAR(100) NOT NULL,
    unit_number     VARCHAR(20)  NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    email           VARCHAR(100),
    address         VARCHAR(200),
    previous_reading BIGINT      NOT NULL DEFAULT 0,
    current_reading  BIGINT      NOT NULL DEFAULT 0,
    last_read_date  TIMESTAMP,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Water bills
CREATE TABLE IF NOT EXISTS water_bills (
    id              BIGSERIAL PRIMARY KEY,
    meter_id        BIGINT       NOT NULL REFERENCES meters(id),
    meter_number    VARCHAR(20)  NOT NULL,
    billing_period  VARCHAR(30)  NOT NULL,
    previous_reading BIGINT      NOT NULL,
    current_reading  BIGINT      NOT NULL,
    usage_litres    BIGINT       NOT NULL,
    units_consumed  DECIMAL(10,2) NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'UNPAID',
    paid_at         TIMESTAMP,
    mpesa_receipt   VARCHAR(50),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- C2B transactions (from M-Pesa paybill)
CREATE TABLE IF NOT EXISTS c2b_transactions (
    id               BIGSERIAL PRIMARY KEY,
    transaction_type VARCHAR(30),
    trans_id         VARCHAR(50)   NOT NULL UNIQUE,
    trans_time       VARCHAR(30),
    trans_amount     DECIMAL(12,2) NOT NULL,
    business_short_code VARCHAR(20),
    bill_ref_number  VARCHAR(50),
    invoice_number   VARCHAR(50),
    org_account_balance DECIMAL(12,2),
    third_party_trans_id VARCHAR(50),
    msisdn           VARCHAR(20)   NOT NULL,
    first_name       VARCHAR(50),
    middle_name      VARCHAR(50),
    last_name        VARCHAR(50),
    status           VARCHAR(20)   NOT NULL DEFAULT 'VALIDATED',
    b2b_disbursed    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- B2B transactions (auto-disbursement)
CREATE TABLE IF NOT EXISTS b2b_transactions (
    id                  BIGSERIAL PRIMARY KEY,
    c2b_transaction_id  BIGINT REFERENCES c2b_transactions(id),
    conversation_id     VARCHAR(100),
    originator_conversation_id VARCHAR(100),
    amount              DECIMAL(12,2) NOT NULL,
    sender_shortcode    VARCHAR(20),
    receiver_shortcode  VARCHAR(20),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    result_code         INTEGER,
    result_desc         VARCHAR(255),
    trans_id            VARCHAR(50),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- STK Push requests
CREATE TABLE IF NOT EXISTS stk_push_requests (
    id                   BIGSERIAL PRIMARY KEY,
    merchant_request_id  VARCHAR(100),
    checkout_request_id  VARCHAR(100) UNIQUE,
    meter_number         VARCHAR(20)  NOT NULL,
    phone                VARCHAR(20)  NOT NULL,
    amount               DECIMAL(12,2) NOT NULL,
    account_reference    VARCHAR(50)  NOT NULL,
    description          VARCHAR(200),
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    result_code          INTEGER,
    result_desc          VARCHAR(255),
    mpesa_receipt_number VARCHAR(50),
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Seed some meters
INSERT INTO meters (meter_number, tenant_name, unit_number, phone, email, address, previous_reading, current_reading, last_read_date, status) VALUES
('M-1001', 'John Smith',     '4A', '+254700010101', 'john@example.com',    'Building A, Floor 4', 9200,  12500, NOW() - INTERVAL '5 days', 'ACTIVE'),
('M-1024', 'Sarah Johnson',  '7B', '+254700010102', 'sarah@example.com',   'Building B, Floor 7', 7600,  9850,  NOW() - INTERVAL '1 day',  'ACTIVE'),
('M-1033', 'Alice Cooper',   '3B', '+254700010106', 'alice@example.com',   'Building B, Floor 3', 9800,  11300, NOW() - INTERVAL '6 days', 'ACTIVE'),
('M-1056', 'Michael Brown',  '2C', '+254700010103', 'michael@example.com', 'Building C, Floor 2', 8100,  15600, NOW() - INTERVAL '7 days', 'ACTIVE'),
('M-1067', 'Bob Martin',     '6C', '+254700010107', 'bob@example.com',     'Building C, Floor 6', 3400,  7650,  NOW() - INTERVAL '8 days', 'ACTIVE'),
('M-1082', 'Carol White',    '8A', '+254700010108', 'carol@example.com',   'Building A, Floor 8', 11200, 13400, NOW() - INTERVAL '1 day',  'ACTIVE'),
('M-1089', 'Emily Davis',    '9A', '+254700010104', 'emily@example.com',   'Building A, Floor 9', 5500,  8720,  NOW() - INTERVAL '1 day',  'ACTIVE'),
('M-1102', 'David Wilson',   '5D', '+254700010105', 'david@example.com',   'Building D, Floor 5', 10000, 14200, NOW() - INTERVAL '3 days', 'ACTIVE'),
('M-1015', 'Dan Green',      '1D', '+254700010109', 'dan@example.com',     'Building D, Floor 1', 7800,  9200,  NOW() - INTERVAL '9 days', 'ACTIVE'),
('M-1045', 'Eva Martinez',   '5A', '+254700010110', 'eva@example.com',     'Building A, Floor 5', 5300,  10800, NOW() - INTERVAL '4 days', 'ACTIVE')
ON CONFLICT (meter_number) DO NOTHING;
