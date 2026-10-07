-- Deterministic baseline for Retailer controller integration tests.

-- Remove dependent order items first.
DELETE FROM order_items
WHERE order_id IN (1, 4)
   OR id IN (1, 6);

-- Remove baseline orders.
DELETE FROM orders
WHERE id IN (1, 4);

-- Remove baseline inventory.
DELETE FROM inventory
WHERE id IN (1, 6);

-- Baseline inventory #1: owned by pspk.
INSERT INTO inventory
(id, product_name, description, quantity, price, retailer_username, created_at)
VALUES
(1, 'samsung fridge', 'double door', 90, 25000, 'pspk', CURRENT_TIMESTAMP);

-- Baseline inventory #6: owned by rrr.
INSERT INTO inventory
(id, product_name, description, quantity, price, retailer_username, created_at)
VALUES
(6, 'cgi', 'integration test inventory', 17, 10000, 'rrr', CURRENT_TIMESTAMP);

-- Baseline order #1: belongs to another user.
INSERT INTO orders
(id, username, grand_total, status, created_at)
VALUES
(1, 'allu', 15000, 'RECEIVED', CURRENT_TIMESTAMP);

-- Baseline order #4: belongs to rr and contains rrr inventory.
INSERT INTO orders
(id, username, grand_total, status, created_at)
VALUES
(4, 'rr', 350000, 'PLACED', CURRENT_TIMESTAMP);

-- Order item #1 for order #1.
INSERT INTO order_items
(id, inventory_id, item_name, quantity, price, total_price, retailer_username, created_at, order_id)
VALUES
(1, 1, 'samsung fridge', 1, 15000, 15000, 'pspk', CURRENT_TIMESTAMP, 1);

-- Order item #6 for order #4.
INSERT INTO order_items
(id, inventory_id, item_name, quantity, price, total_price, retailer_username, created_at, order_id)
VALUES
(6, 6, 'cgi', 2, 10000, 20000, 'rrr', CURRENT_TIMESTAMP, 4);

-- Keep identity sequences above the explicit baseline IDs.
SELECT setval(
    pg_get_serial_sequence('inventory', 'id'),
    COALESCE((SELECT MAX(id) FROM inventory), 1),
    true
);

SELECT setval(
    pg_get_serial_sequence('orders', 'id'),
    COALESCE((SELECT MAX(id) FROM orders), 1),
    true
);

SELECT setval(
    pg_get_serial_sequence('order_items', 'id'),
    COALESCE((SELECT MAX(id) FROM order_items), 1),
    true
);
