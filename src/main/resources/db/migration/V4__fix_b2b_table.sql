-- Add missing columns to b2b_transactions
ALTER TABLE b2b_transactions
    ADD COLUMN IF NOT EXISTS c2b_transaction_id BIGINT,
    ADD COLUMN IF NOT EXISTS stk_push_request_id BIGINT,
    ADD COLUMN IF NOT EXISTS source_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS originator_conversation_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS account_reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS result_code INTEGER,
    ADD COLUMN IF NOT EXISTS result_desc TEXT,
    ADD COLUMN IF NOT EXISTS trans_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;