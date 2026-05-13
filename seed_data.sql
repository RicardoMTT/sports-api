-- ============================================================
-- SCRIPT DE DATOS DE PRUEBA - Music Store API
-- Base de datos: MusicStoreDB (Azure SQL)
--
-- CREDENCIALES DE PRUEBA (todas las cuentas usan la misma):
--   Contrasena: password123
--
-- USUARIOS:
--   ADMIN    → admin@musicstore.com  / password123
--   CUSTOMER → juan@email.com        / password123
--   CUSTOMER → maria@email.com       / password123
--   CUSTOMER → carlos@email.com      / password123
-- ============================================================


-- ============================================================
-- 0. CORRECCION DE CHECK CONSTRAINT
--    Hibernate creó este constraint con los valores viejos del enum
--    (FOOTWEAR, CLOTHING, EQUIPMENT). Se elimina y se recrea con
--    los nuevos valores musicales.
-- ============================================================
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CK__products__catego__7C4F7684'
)
    ALTER TABLE products DROP CONSTRAINT CK__products__catego__7C4F7684;

-- Eliminar cualquier otro CHECK constraint sobre la columna category
-- por si Hibernate generó uno con nombre distinto en tu entorno
DECLARE @constraintName NVARCHAR(256);
SELECT @constraintName = cc.name
FROM sys.check_constraints cc
JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id
JOIN sys.tables t  ON cc.parent_object_id = t.object_id
WHERE t.name = 'products' AND c.name = 'category';

IF @constraintName IS NOT NULL
    EXEC('ALTER TABLE products DROP CONSTRAINT ' + @constraintName);

-- Crear el constraint con las categorias actuales
ALTER TABLE products
    ADD CONSTRAINT CK_products_category
    CHECK (category IN ('VINYL','CD','CLOTHING','ACCESSORIES','INSTRUMENTS','POSTERS','BOOKS'));


-- ============================================================
-- 1. LIMPIEZA (orden inverso a FK constraints)
-- ============================================================
DELETE FROM audit_logs;
DELETE FROM idempotency_keys;
DELETE FROM refresh_tokens;
DELETE FROM cart_items;
DELETE FROM order_items;
DELETE FROM carts;
DELETE FROM orders;
DELETE FROM products;
DELETE FROM users;

-- Reiniciar contadores de identidad
DBCC CHECKIDENT ('audit_logs',       RESEED, 0);
DBCC CHECKIDENT ('idempotency_keys', RESEED, 0);
DBCC CHECKIDENT ('refresh_tokens',   RESEED, 0);
DBCC CHECKIDENT ('cart_items',       RESEED, 0);
DBCC CHECKIDENT ('order_items',      RESEED, 0);
DBCC CHECKIDENT ('carts',            RESEED, 0);
DBCC CHECKIDENT ('orders',           RESEED, 0);
DBCC CHECKIDENT ('products',         RESEED, 0);
DBCC CHECKIDENT ('users',            RESEED, 0);


-- ============================================================
-- 2. USUARIOS
-- BCrypt de "password123" (strength 10)
-- ============================================================
INSERT INTO users (first_name, last_name, email, password, role, created_at, updated_at) VALUES
('Admin',   'Music',   'admin@musicstore.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN',    GETDATE(), GETDATE()),
('Juan',    'Perez',   'juan@email.com',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CUSTOMER', GETDATE(), GETDATE()),
('Maria',   'Lopez',   'maria@email.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CUSTOMER', GETDATE(), GETDATE()),
('Carlos',  'Ruiz',    'carlos@email.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CUSTOMER', GETDATE(), GETDATE());
-- IDs resultantes: admin=1, juan=2, maria=3, carlos=4


-- ============================================================
-- 3. PRODUCTOS (todas las categorias del enum)
-- ============================================================
INSERT INTO products (name, brand, price, stock, category, version, image_url, created_at, updated_at) VALUES
-- VINYL
('Dark Side of the Moon', 'Pink Floyd',    29.99,  50, 'VINYL',       0, 'https://placehold.co/400x400?text=Dark+Side',    GETDATE(), GETDATE()),
('Abbey Road',            'The Beatles',   27.99,  40, 'VINYL',       0, 'https://placehold.co/400x400?text=Abbey+Road',   GETDATE(), GETDATE()),
('Rumours',               'Fleetwood Mac', 25.99,  35, 'VINYL',       0, 'https://placehold.co/400x400?text=Rumours',      GETDATE(), GETDATE()),
('Led Zeppelin IV',       'Led Zeppelin',  28.99,  30, 'VINYL',       0, 'https://placehold.co/400x400?text=LZ+IV',        GETDATE(), GETDATE()),
-- CD
('Nevermind',             'Nirvana',       14.99,  60, 'CD',          0, 'https://placehold.co/400x400?text=Nevermind',    GETDATE(), GETDATE()),
('OK Computer',           'Radiohead',     13.99,  45, 'CD',          0, 'https://placehold.co/400x400?text=OK+Computer',  GETDATE(), GETDATE()),
('The Chronic',           'Dr. Dre',       12.99,  55, 'CD',          0, 'https://placehold.co/400x400?text=The+Chronic',  GETDATE(), GETDATE()),
-- CLOTHING
('Black Album Tour Tee',  'Metallica',     34.99, 100, 'CLOTHING',    0, 'https://placehold.co/400x400?text=Metallica+Tee',GETDATE(), GETDATE()),
('Dark Side Hoodie',      'Pink Floyd',    59.99,  80, 'CLOTHING',    0, 'https://placehold.co/400x400?text=PF+Hoodie',    GETDATE(), GETDATE()),
('Vintage Logo Tee',      'Led Zeppelin',  29.99,  90, 'CLOTHING',    0, 'https://placehold.co/400x400?text=LZ+Tee',       GETDATE(), GETDATE()),
-- ACCESSORIES
('Guitar Pick Set 12pcs', 'Dunlop',         8.99, 200, 'ACCESSORIES', 0, 'https://placehold.co/400x400?text=Picks',        GETDATE(), GETDATE()),
('Leather Guitar Strap',  'Levy''s',       45.99,  70, 'ACCESSORIES', 0, 'https://placehold.co/400x400?text=Strap',        GETDATE(), GETDATE()),
('Music Note Beanie',     'Generic',       19.99, 150, 'ACCESSORIES', 0, 'https://placehold.co/400x400?text=Beanie',       GETDATE(), GETDATE()),
-- INSTRUMENTS
('Acoustic Guitar',       'Fender',       299.99,  15, 'INSTRUMENTS', 0, 'https://placehold.co/400x400?text=Guitar',       GETDATE(), GETDATE()),
('Keyboard 61 Keys',      'Yamaha',       199.99,  20, 'INSTRUMENTS', 0, 'https://placehold.co/400x400?text=Keyboard',     GETDATE(), GETDATE()),
-- POSTERS
('Nevermind Poster A2',   'Nirvana',       12.99, 300, 'POSTERS',     0, 'https://placehold.co/400x400?text=NV+Poster',    GETDATE(), GETDATE()),
('Abbey Road Poster A2',  'The Beatles',    9.99, 300, 'POSTERS',     0, 'https://placehold.co/400x400?text=AR+Poster',    GETDATE(), GETDATE()),
-- BOOKS
('Guitar for Beginners',  'Hal Leonard',   24.99,  60, 'BOOKS',       0, 'https://placehold.co/400x400?text=Guitar+Book',  GETDATE(), GETDATE()),
('The History of Rock',   'Various',       19.99,  50, 'BOOKS',       0, 'https://placehold.co/400x400?text=Rock+History', GETDATE(), GETDATE());
-- IDs resultantes: 1-4=VINYL, 5-7=CD, 8-10=CLOTHING, 11-13=ACCESSORIES, 14-15=INSTRUMENTS, 16-17=POSTERS, 18-19=BOOKS


-- ============================================================
-- 4. CARRITO ACTIVO (Juan - user_id=2)
--    Simula un usuario con items en el carrito listo para comprar
-- ============================================================
INSERT INTO carts (user_id, total_price, created_at, updated_at) VALUES
(2, 0.00, GETDATE(), GETDATE());
-- cart_id=1

INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
(1, 1,  1),   -- Dark Side of the Moon (VINYL)  x1 = $29.99
(1, 8,  2),   -- Metallica Tee (CLOTHING)        x2 = $69.98
(1, 11, 3);   -- Guitar Pick Set (ACCESSORIES)   x3 = $26.97

-- Total = 29.99 + 69.98 + 26.97 = $126.94
UPDATE carts SET total_price = 126.94, updated_at = GETDATE() WHERE id = 1;


-- ============================================================
-- 5. ORDENES en distintos estados
-- ============================================================
INSERT INTO orders (user_id, status, total_amount, created_at, updated_at) VALUES
(2, 'PAID',      38.97, DATEADD(DAY, -5,  GETDATE()), DATEADD(DAY, -5,  GETDATE())),  -- id=1
(2, 'SHIPPED',   59.99, DATEADD(DAY, -10, GETDATE()), DATEADD(DAY, -9,  GETDATE())),  -- id=2
(3, 'PENDING',   46.97, DATEADD(DAY, -1,  GETDATE()), DATEADD(DAY, -1,  GETDATE())),  -- id=3
(3, 'CANCELLED', 13.99, DATEADD(DAY, -15, GETDATE()), DATEADD(DAY, -14, GETDATE())),  -- id=4
(4, 'PAID',     324.98, DATEADD(DAY, -3,  GETDATE()), DATEADD(DAY, -3,  GETDATE()));  -- id=5

-- Items de orden 1 — PAID (Juan): Nevermind CD x2 + Guitar Picks x1
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1, 5,  2, 14.99),   -- Nevermind x2 = $29.98
(1, 11, 1,  8.99);   -- Guitar Picks x1 = $8.99   → total $38.97

-- Items de orden 2 — SHIPPED (Juan): Dark Side Hoodie x1
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(2, 9,  1, 59.99);   -- Dark Side Hoodie = $59.99

-- Items de orden 3 — PENDING (Maria): OK Computer + Beanie + Nevermind Poster
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(3, 6,  1, 13.99),   -- OK Computer = $13.99
(3, 13, 1, 19.99),   -- Music Note Beanie = $19.99
(3, 16, 1, 12.99);   -- Nevermind Poster = $12.99  → total $46.97

-- Items de orden 4 — CANCELLED (Maria): OK Computer CD x1
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(4, 6,  1, 13.99);   -- OK Computer = $13.99

-- Items de orden 5 — PAID (Carlos): Acoustic Guitar + Guitar Picks x5
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(5, 14, 1, 299.99),  -- Acoustic Guitar = $299.99
(5, 11, 5,   4.99);  -- Guitar Picks x5 = $24.99  → total $324.98


-- ============================================================
-- 6. AUDIT LOGS
-- ============================================================
INSERT INTO audit_logs (operation, reason, timestamp) VALUES
('ORDER_CREATED',   'User 2 placed order 1',                   DATEADD(DAY, -5,  GETDATE())),
('ORDER_PAID',      'Payment confirmed for order 1',            DATEADD(DAY, -5,  GETDATE())),
('ORDER_CREATED',   'User 2 placed order 2',                   DATEADD(DAY, -10, GETDATE())),
('ORDER_PAID',      'Payment confirmed for order 2',            DATEADD(DAY, -10, GETDATE())),
('ORDER_SHIPPED',   'Order 2 dispatched to carrier',            DATEADD(DAY, -9,  GETDATE())),
('ORDER_CREATED',   'User 3 placed order 3',                   DATEADD(DAY, -1,  GETDATE())),
('ORDER_CREATED',   'User 3 placed order 4',                   DATEADD(DAY, -15, GETDATE())),
('ORDER_CANCELLED', 'User 3 cancelled order 4 - changed mind',  DATEADD(DAY, -14, GETDATE())),
('ORDER_CREATED',   'User 4 placed order 5',                   DATEADD(DAY, -3,  GETDATE())),
('ORDER_PAID',      'Payment confirmed for order 5',            DATEADD(DAY, -3,  GETDATE()));


-- ============================================================
-- 7. VERIFICACION FINAL
-- ============================================================
SELECT 'users'        AS tabla, COUNT(*) AS registros FROM users
UNION ALL
SELECT 'products',     COUNT(*) FROM products
UNION ALL
SELECT 'carts',        COUNT(*) FROM carts
UNION ALL
SELECT 'cart_items',   COUNT(*) FROM cart_items
UNION ALL
SELECT 'orders',       COUNT(*) FROM orders
UNION ALL
SELECT 'order_items',  COUNT(*) FROM order_items
UNION ALL
SELECT 'audit_logs',   COUNT(*) FROM audit_logs;
