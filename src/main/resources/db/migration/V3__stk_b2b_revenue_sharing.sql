-- V3: Support STK-triggered B2B revenue sharing
-- Add stk_push_request_id to b2b_transactions so B2B can be linked to either C2B or STK
ALTER TABLE b2b_transactions ADD COLUMN IF NOT EXISTS stk_push_request_id BIGINT REFERENCES stk_push_requests(id);
ALTER TABLE b2b_transactions ADD COLUMN IF NOT EXISTS source_type VARCHAR(10) DEFAULT 'C2B';
ALTER TABLE b2b_transactions ADD COLUMN IF NOT EXISTS account_reference VARCHAR(100);

-- Track whether revenue sharing was triggered from the STK push
ALTER TABLE stk_push_requests ADD COLUMN IF NOT EXISTS b2b_disbursed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE stk_push_requests ADD COLUMN IF NOT EXISTS b2b_transaction_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_b2b_stk_id ON b2b_transactions(stk_push_request_id);
CREATE INDEX IF NOT EXISTS idx_stk_status ON stk_push_requests(status);
CREATE INDEX IF NOT EXISTS idx_stk_meter ON stk_push_requests(meter_number);
