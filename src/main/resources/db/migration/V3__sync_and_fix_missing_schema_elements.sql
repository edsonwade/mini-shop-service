-- V3: Add missing columns, indices, and security tables

-- 1. Add tenant_id to payments and coupons (previously forgotten columns)
ALTER TABLE payments ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
-- Fill existing records with a default if needed, or just set to NOT NULL after adding
-- UPDATE payments SET tenant_id = 'DEFAULT' WHERE tenant_id IS NULL;
-- ALTER TABLE payments ALTER COLUMN tenant_id SET NOT NULL;

ALTER TABLE coupons ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
-- UPDATE coupons SET tenant_id = 'DEFAULT' WHERE tenant_id IS NULL;
-- ALTER TABLE coupons ALTER COLUMN tenant_id SET NOT NULL;

-- 2. Security: Create explicit permissions table and join table for role-based permissions
-- Even though JPA currently handles this via Enum, this aligns the DB with the domain model
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role VARCHAR(50) NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role, permission_id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

-- 3. Add indices for foreign keys to improve performance
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_tenant_id ON orders(tenant_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_products_tenant_id ON products(tenant_id);
CREATE INDEX IF NOT EXISTS idx_customers_tenant_id ON customers(tenant_id);

-- 4. Add unique constraint on user_roles to prevent duplicates
ALTER TABLE user_roles ADD CONSTRAINT unique_user_role UNIQUE (user_id, role);

-- 5. Seed permissions (matching Permission enum)
INSERT INTO permissions (id, name) VALUES 
(gen_random_uuid(), 'PLACE_ORDER'),
(gen_random_uuid(), 'CANCEL_ORDER'),
(gen_random_uuid(), 'SETTLE_ORDER'),
(gen_random_uuid(), 'APPLY_COUPON'),
(gen_random_uuid(), 'MANAGE_PRODUCTS'),
(gen_random_uuid(), 'MANAGE_PROMOTIONS'),
(gen_random_uuid(), 'VIEW_ANALYTICS'),
(gen_random_uuid(), 'MANAGE_USERS')
ON CONFLICT (name) DO NOTHING;

-- Seed role_permissions based on Role.java
-- ADMIN role permissions
INSERT INTO role_permissions (role, permission_id)
SELECT 'ADMIN', id FROM permissions WHERE name IN ('PLACE_ORDER', 'CANCEL_ORDER', 'SETTLE_ORDER', 'APPLY_COUPON', 'MANAGE_PRODUCTS', 'MANAGE_PROMOTIONS', 'VIEW_ANALYTICS', 'MANAGE_USERS')
ON CONFLICT DO NOTHING;

-- OPERATOR role permissions
INSERT INTO role_permissions (role, permission_id)
SELECT 'OPERATOR', id FROM permissions WHERE name IN ('PLACE_ORDER', 'CANCEL_ORDER', 'SETTLE_ORDER', 'MANAGE_PRODUCTS')
ON CONFLICT DO NOTHING;

-- CUSTOMER role permissions
INSERT INTO role_permissions (role, permission_id)
SELECT 'CUSTOMER', id FROM permissions WHERE name IN ('PLACE_ORDER', 'CANCEL_ORDER', 'APPLY_COUPON')
ON CONFLICT DO NOTHING;
