-- Add missing columns to orders table
ALTER TABLE orders ADD COLUMN IF NOT EXISTS coupon_code VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS discount_amount DECIMAL(19, 2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS discount_currency VARCHAR(3);

-- Add missing user_recovery_codes table
CREATE TABLE IF NOT EXISTS user_recovery_codes (
    user_id UUID NOT NULL,
    code VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, code),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
