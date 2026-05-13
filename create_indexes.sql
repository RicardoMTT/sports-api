-- ============================================================
-- SCRIPT DE ÍNDICES — Soundstore API
-- Base de datos: SportsStoreDB (Azure SQL / SQL Server)
--
-- Ejecutar UNA SOLA VEZ sobre la base de datos de producción.
-- Todos los índices usan IF NOT EXISTS para ser re-ejecutables
-- sin error si ya existen.
--
-- Tablas cubiertas:
--   products, orders, order_items, carts,
--   refresh_tokens, idempotency_keys, audit_logs
-- ============================================================


-- ============================================================
-- 1. PRODUCTS
--    Consultas frecuentes:
--      · findByCategory          → WHERE category = ?
--      · searchProductsWithLike  → WHERE LOWER(name) LIKE ?
--      · findAll (sort by name)  → ORDER BY name
--      · findAll (sort by price) → ORDER BY price
-- ============================================================

-- Filtro por categoría (el filtro más usado en el frontend)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_products_category'
    AND object_id = OBJECT_ID('products')
)
    CREATE INDEX IX_products_category
    ON products (category)
    INCLUDE (name, brand, price, stock, image_url);
-- INCLUDE evita un segundo acceso a la tabla para traer
-- las columnas que el frontend necesita mostrar en la grid.

-- Búsqueda por nombre (buscador del frontend)
-- SQL Server no soporta índices funcionales sobre LOWER(),
-- pero un índice sobre name igual reduce el scan considerablemente
-- cuando la selectividad del LIKE es alta (ej: "Abbey%").
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_products_name'
    AND object_id = OBJECT_ID('products')
)
    CREATE INDEX IX_products_name
    ON products (name)
    INCLUDE (brand, price, stock, category, image_url);

-- Ordenación por precio (slider + sort del frontend)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_products_price'
    AND object_id = OBJECT_ID('products')
)
    CREATE INDEX IX_products_price
    ON products (price ASC)
    INCLUDE (name, brand, stock, category, image_url);


-- ============================================================
-- 2. ORDERS
--    Consultas frecuentes:
--      · findMyOrders        → WHERE user_id = ? ORDER BY created_at DESC
--      · findByIdAndUserId   → WHERE id = ? AND user_id = ?
-- ============================================================

-- Historial de órdenes por usuario (página "Mis pedidos")
-- El ORDER BY created_at DESC queda cubierto por el índice compuesto.
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_orders_userId_createdAt'
    AND object_id = OBJECT_ID('orders')
)
    CREATE INDEX IX_orders_userId_createdAt
    ON orders (user_id ASC, created_at DESC)
    INCLUDE (status, total_amount);

-- Filtro por estado (tabs PAID / PENDING / CANCELLED del frontend)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_orders_status'
    AND object_id = OBJECT_ID('orders')
)
    CREATE INDEX IX_orders_status
    ON orders (status)
    INCLUDE (user_id, total_amount, created_at);


-- ============================================================
-- 3. ORDER_ITEMS
--    Consultas frecuentes:
--      · LEFT JOIN FETCH o.items → WHERE order_id = ?
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_order_items_orderId'
    AND object_id = OBJECT_ID('order_items')
)
    CREATE INDEX IX_order_items_orderId
    ON order_items (order_id)
    INCLUDE (product_id, quantity, unit_price);


-- ============================================================
-- 4. CARTS
--    Consultas frecuentes:
--      · findByUserId → WHERE user_id = ?
--        (cada operación del carrito: agregar, quitar, checkout)
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_carts_userId'
    AND object_id = OBJECT_ID('carts')
)
    CREATE UNIQUE INDEX IX_carts_userId
    ON carts (user_id);
-- UNIQUE porque cada usuario tiene exactamente un carrito.
-- Esto además garantiza integridad a nivel de BD.


-- ============================================================
-- 5. CART_ITEMS
--    Consultas frecuentes:
--      · cart.getItems() filtrado por product_id
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_cart_items_cartId'
    AND object_id = OBJECT_ID('cart_items')
)
    CREATE INDEX IX_cart_items_cartId
    ON cart_items (cart_id)
    INCLUDE (product_id, quantity);


-- ============================================================
-- 6. REFRESH_TOKENS
--    Consultas frecuentes:
--      · findByToken          → WHERE token = ?  (cada request autenticado)
--      · revokeAllByUserId    → WHERE user_id = ? AND revoked = false
--      · deleteExpiredAndRevoked → WHERE expires_at < NOW() OR revoked = true
-- ============================================================

-- Búsqueda por valor del token (la más crítica — ocurre en cada refresh)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_refresh_tokens_token'
    AND object_id = OBJECT_ID('refresh_tokens')
)
    CREATE UNIQUE INDEX IX_refresh_tokens_token
    ON refresh_tokens (token);
-- UNIQUE porque cada token es irrepetible por diseño.

-- Revocación por usuario (logout)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_refresh_tokens_userId_revoked'
    AND object_id = OBJECT_ID('refresh_tokens')
)
    CREATE INDEX IX_refresh_tokens_userId_revoked
    ON refresh_tokens (user_id, revoked)
    INCLUDE (expires_at);

-- Limpieza periódica de tokens expirados (@Scheduled)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_refresh_tokens_expiresAt_revoked'
    AND object_id = OBJECT_ID('refresh_tokens')
)
    CREATE INDEX IX_refresh_tokens_expiresAt_revoked
    ON refresh_tokens (expires_at, revoked);


-- ============================================================
-- 7. IDEMPOTENCY_KEYS
--    Consultas frecuentes:
--      · findByKeyValue → WHERE key_value = ?  (cada checkout)
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_idempotency_keys_keyValue'
    AND object_id = OBJECT_ID('idempotency_keys')
)
    CREATE UNIQUE INDEX IX_idempotency_keys_keyValue
    ON idempotency_keys (key_value);
-- Ya existe un UNIQUE constraint desde Hibernate, pero hacerlo
-- índice explícito permite al query optimizer usarlo más eficientemente.


-- ============================================================
-- 8. AUDIT_LOGS
--    Consultas frecuentes:
--      · búsquedas por operación o por rango de fecha (admin)
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_audit_logs_timestamp'
    AND object_id = OBJECT_ID('audit_logs')
)
    CREATE INDEX IX_audit_logs_timestamp
    ON audit_logs (timestamp DESC)
    INCLUDE (operation, reason);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_audit_logs_operation'
    AND object_id = OBJECT_ID('audit_logs')
)
    CREATE INDEX IX_audit_logs_operation
    ON audit_logs (operation)
    INCLUDE (timestamp, reason);


-- ============================================================
-- VERIFICACIÓN FINAL
-- Lista todos los índices creados por este script
-- ============================================================
SELECT
    t.name          AS tabla,
    i.name          AS indice,
    i.type_desc     AS tipo,
    i.is_unique     AS es_unico,
    STRING_AGG(c.name, ', ') WITHIN GROUP (ORDER BY ic.key_ordinal) AS columnas_clave
FROM sys.indexes i
JOIN sys.tables t ON i.object_id = t.object_id
JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE t.name IN ('products','orders','order_items','carts','cart_items',
                 'refresh_tokens','idempotency_keys','audit_logs')
  AND i.name LIKE 'IX_%'
  AND ic.is_included_column = 0
GROUP BY t.name, i.name, i.type_desc, i.is_unique
ORDER BY t.name, i.name;
