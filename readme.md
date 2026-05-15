# 🎵 Soundstore API

API REST para la tienda de música **Soundstore** — vinilos, CDs, instrumentos, ropa, accesorios, posters y libros.

**Producción:** [`https://sports-api-back-zd0c.onrender.com`](https://sports-api-back-zd0c.onrender.com)  
**Swagger UI:** [`https://sports-api-back-zd0c.onrender.com/swagger-ui/index.html`](https://sports-api-back-zd0c.onrender.com/swagger-ui/index.html)

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.5 |
| Seguridad | Spring Security + JWT (JJWT 0.11.5) |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | Microsoft SQL Server (Azure) |
| Pool de conexiones | HikariCP |
| Documentación | SpringDoc OpenAPI 3 / Swagger UI 2.8.6 |
| Monitoreo | Spring Boot Actuator |
| Build | Maven |

---

## Arquitectura del proyecto

```
src/main/java/com/musicstore/music_api/
├── config/
│   ├── ApplicationConfig.java        # Beans de Spring: UserDetailsService, PasswordEncoder, AuthProvider
│   ├── JpaAuditingConfig.java        # Habilita @CreatedDate / @LastModifiedDate
│   ├── JwtAuthenticationFilter.java  # Filtro JWT — valida el token en cada request
│   ├── OpenApiConfig.java            # Configuración de Swagger UI con auth Bearer
│   └── SecurityConfig.java          # Rutas públicas vs protegidas, CORS
├── controller/
│   ├── AuthController.java           # /api/v1/auth/**
│   ├── ProductController.java        # /api/v1/products/**
│   ├── ShoppingController.java       # /api/v1/shopping/**
│   ├── OrderController.java          # /api/v1/orders/**
│   └── HealthController.java         # /api/v1/health/**
├── domain/
│   ├── dtos/                         # Records de request/response (DTOs)
│   ├── entities/                     # Entidades JPA
│   ├── enums/                        # Category, OrderStatus, Role
│   └── event/                        # OrderCompletedEvent (Spring Events)
├── exception/
│   ├── GlobalExceptionHandler.java   # Manejo centralizado de errores
│   ├── ResourceNotFoundException.java
│   ├── InsufficientStockException.java
│   └── IdempotencyConflictException.java
├── listener/
│   └── NotificationListener.java     # Escucha OrderCompletedEvent
├── repository/                       # Interfaces JPA Repository
└── services/                         # Lógica de negocio
    ├── AuthenticationService.java
    ├── JwtService.java
    ├── RefreshTokenService.java
    ├── ProductService.java
    ├── ShoppingService.java
    ├── OrderService.java
    ├── IdempotencyService.java
    └── AuditService.java
```

---

## Endpoints de la API

### 🔐 Autenticación — `/api/v1/auth`

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `POST` | `/register` | Público | Crea cuenta CUSTOMER, devuelve JWT |
| `POST` | `/login` | Público | Autentica y devuelve access + refresh token |
| `POST` | `/refresh` | Público | Genera nuevo access token con el refresh token |
| `POST` | `/logout` | JWT | Revoca el refresh token en base de datos |

**Login — ejemplo de respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716..."
}
```
- `token`: JWT de acceso, expira en **15 minutos**
- `refreshToken`: UUID opaco, expira en **7 días**

---

### 📦 Productos — `/api/v1/products`

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/` | Público | Lista paginada. Filtra por `name` o `category` |
| `GET` | `/{id}` | Público | Detalle de un producto |
| `GET` | `/category/{category}` | Público | Lista por categoría (path variable) |
| `POST` | `/` | ADMIN | Crea un nuevo producto |

**Categorías disponibles:** `VINYL` · `CD` · `CLOTHING` · `ACCESSORIES` · `INSTRUMENTS` · `POSTERS` · `BOOKS`

**Paginación por defecto:** `page=0`, `size=6`, `sort=name`

**Ejemplo request — crear producto (ADMIN):**
```json
{
  "name": "Dark Side of the Moon",
  "brand": "Pink Floyd",
  "price": 29.99,
  "stock": 50,
  "category": "VINYL",
  "imageUrl": "https://ejemplo.com/dark-side.jpg"
}
```

---

### 🛒 Carrito & Checkout — `/api/v1/shopping`

Todos los endpoints requieren JWT.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/cart/items` | Agrega un producto al carrito (o incrementa cantidad) |
| `PUT` | `/cart/items/{productId}` | Actualiza la cantidad de un item |
| `DELETE` | `/cart/items/{productId}` | Elimina un item del carrito |
| `DELETE` | `/cart` | Vacía el carrito completo |
| `POST` | `/checkout` | Procesa el carrito como una orden |

**Checkout — header obligatorio:**
```
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
```
Generá un UUID único por intento. Si enviás la misma clave dos veces, la segunda devuelve el resultado cacheado en lugar de procesar el pago de nuevo (protección contra doble cobro).

---

### 📋 Órdenes — `/api/v1/orders`

Todos los endpoints requieren JWT. El usuario se identifica por el token — no es posible consultar órdenes de otro usuario.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/my` | Historial paginado (más reciente primero) |
| `GET` | `/{orderId}` | Detalle completo con items |

**Estados de una orden:** `PENDING` → `PAID` → `SHIPPED` · `CANCELLED`

---

### 🏥 Health Checks — `/api/v1/health`

Endpoints públicos — no requieren autenticación.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/ping` | Ping básico — solo verifica que la app está levantada |
| `GET` | `/` | Health detallado — verifica app + conexión a BD |

**Respuesta normal (`200 OK`):**
```json
{
  "status": "UP",
  "version": "1.0.0",
  "timestamp": "2026-05-13T14:32:10.123Z",
  "uptime": "2h 15m 43s",
  "database": {
    "status": "UP",
    "type": "SQL Server"
  }
}
```

**BD caída (`503 Service Unavailable`):**
```json
{
  "status": "DEGRADED",
  "database": {
    "status": "DOWN",
    "error": "Connection refused..."
  }
}
```

**Actuator (estándar Spring Boot):**

| Ruta | Auth | Descripción |
|---|---|---|
| `/actuator/health` | Público (resumen) / ADMIN (detalle) | Estado de componentes Spring |
| `/actuator/info` | Público | Versión y descripción de la app |

---

## Seguridad

### Flujo de autenticación

```
1. POST /auth/login  →  { token, refreshToken }
2. Requests protegidos:  Authorization: Bearer <token>
3. Token expirado (401)  →  POST /auth/refresh con refreshToken
4. POST /auth/logout     →  revoca refreshToken en BD
```

### Roles

| Rol | Permisos |
|---|---|
| `CUSTOMER` | Carrito, checkout, ver sus propias órdenes |
| `ADMIN` | Todo lo anterior + crear productos, ver health detallado de Actuator |

### Credenciales de prueba

| Rol | Email | Contraseña |
|---|---|---|
| ADMIN | `admin@musicstore.com` | `password` |
| CUSTOMER | `juan@email.com` | `password` |

---

## Características técnicas

### Control de concurrencia — doble capa

El descuento de stock durante el checkout usa dos mecanismos combinados para eliminar race conditions:

**Nivel 3 — UPDATE atómico en base de datos (protección principal)**

```sql
UPDATE products
SET stock = stock - :qty
WHERE id = :id AND stock >= :qty
```

La resta y la verificación ocurren en una sola instrucción dentro del motor SQL. SQL Server aplica un row-level lock automático sobre la fila, por lo que si dos usuarios llegan al mismo tiempo, uno ejecuta y el otro espera microsegundos hasta que el primero termina. Cuando el segundo ejecuta, el `WHERE stock >= qty` ya no se cumple y afecta 0 filas — la aplicación detecta eso y lanza `InsufficientStockException` (409).

No existe ventana de tiempo entre leer y escribir porque **nunca se lee el stock en Java para hacer la resta** — todo ocurre en la BD de forma atómica.

**Nivel 2 — `@Version` / Optimistic Locking (protección secundaria)**

```java
@Version
private Long version;
```

El campo `version` en `Product` protege contra modificaciones concurrentes de **otros campos** (precio, nombre, categoría) que un administrador pudiera estar cambiando mientras un usuario hace checkout. Hibernate incluye `AND version = :v` en cada `UPDATE` automáticamente — si la versión no coincide, lanza `ObjectOptimisticLockingFailureException` (409).

**¿Por qué no `synchronized` o `AtomicInteger`?**

Esos mecanismos operan en memoria RAM dentro de un único proceso Java. Si la aplicación corre en más de un servidor (escalado horizontal), cada instancia tiene su propia memoria y no se conocen entre sí — la race condition volvería a existir. La BD es la única fuente de verdad compartida entre todos los servidores, por lo que la protección siempre debe estar ahí.

### Idempotencia en checkout
La tabla `idempotency_keys` guarda cada `Idempotency-Key` recibida. Si llega duplicada:
- Estado `PROCESSING` → devuelve `409` (la transacción anterior aún está en curso)
- Estado `COMPLETED` → devuelve el resultado cacheado sin reprocesar

### Refresh Token Rotation
Los refresh tokens se almacenan en BD (tabla `refresh_tokens`) con campos `expiresAt` y `revoked`. Al hacer logout, el token se marca como revocado y no puede usarse aunque no haya expirado.

### Identidad por JWT (no por parámetro)
`OrderController` extrae el email del usuario directamente del JWT validado, sin aceptar parámetros del cliente. Esto impide que un usuario consulte órdenes de otro usuario manipulando la URL.

### Auditoría automática
Las entidades que extienden `Auditable` registran automáticamente `createdAt` y `updatedAt` con `@EnableJpaAuditing`. Eventos importantes se registran también en la tabla `audit_logs`.

### Manejo de errores centralizado
`GlobalExceptionHandler` captura y normaliza todas las excepciones. Todos los errores tienen el mismo formato:

```json
{
  "timestamp": "2026-05-13T14:32:10",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado"
}
```

| Excepción | HTTP |
|---|---|
| `ResourceNotFoundException` | 404 |
| `InsufficientStockException` | 409 |
| `IdempotencyConflictException` | 409 |
| `ObjectOptimisticLockingFailureException` | 409 |
| `BadCredentialsException` | 401 |
| `MethodArgumentNotValidException` | 400 |
| `MissingServletRequestParameterException` | 400 |
| `IllegalStateException` | 400 |

---

## Variables de entorno

| Variable | Descripción | Default (desarrollo) |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL de SQL Server | `jdbc:sqlserver://localhost:1433;...` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de BD | `sa` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de BD | — |
| `JWT_SECRET_KEY` | Clave HMAC-SHA256 en hex (mín. 256 bits) | Valor de desarrollo |

---

## Ejecutar localmente

### Prerrequisitos
- Java 17+
- Maven 3.8+
- SQL Server (local o Docker)

### Pasos

```bash
# 1. Clonar el repositorio
git clone <repo-url>
cd sports-api

# 2. Configurar variables de entorno
export SPRING_DATASOURCE_URL="jdbc:sqlserver://localhost:1433;databaseName=SoundstoreDB;encrypt=true;trustServerCertificate=true;"
export SPRING_DATASOURCE_USERNAME="sa"
export SPRING_DATASOURCE_PASSWORD="tu_password"
export JWT_SECRET_KEY="tu_clave_hex_256bits"

# 3. Compilar y ejecutar
mvn spring-boot:run

# La API queda disponible en http://localhost:8081
```

### SQL Server con Docker

```bash
docker run -e "ACCEPT_EULA=Y" \
           -e "SA_PASSWORD=TuPassword123!" \
           -p 1433:1433 \
           mcr.microsoft.com/mssql/server:2022-latest
```

---

## Documentación interactiva (Swagger UI)

Accedé a la documentación en:

- **Local:** `http://localhost:8081/swagger-ui/index.html`
- **Producción:** `https://sports-api-back-zd0c.onrender.com/swagger-ui/index.html`

Para probar endpoints protegidos:
1. Ejecutá `POST /api/v1/auth/login` y copiá el campo `token`
2. Hacé clic en **Authorize** (arriba a la derecha en Swagger UI)
3. Pegá el token (sin el prefijo `Bearer`)

---

## Índices de base de datos

El archivo `create_indexes.sql` en la raíz del proyecto contiene 14 índices B-Tree optimizados para las consultas más frecuentes. Todos usan `IF NOT EXISTS` para ser seguros de re-ejecutar.

```sql
-- Índices destacados
IX_products_category        -- filtrado por categoría (INCLUDE de columnas frecuentes)
IX_orders_userId_createdAt  -- historial del usuario ordenado por fecha (compuesto)
IX_carts_userId             -- carrito activo del usuario (UNIQUE)
IX_refresh_tokens_token     -- lookup de refresh token (UNIQUE)
```

---

## Modelo de datos

```
users ──────────────┬── carts ── cart_items ── products
                    │
                    ├── orders ── order_items ── products
                    │
                    ├── refresh_tokens
                    └── audit_logs

idempotency_keys    (independiente — guarda el resultado del checkout)
```

| Tabla | Descripción |
|---|---|
| `users` | Cuentas con rol `CUSTOMER` o `ADMIN` |
| `products` | Catálogo con UPDATE atómico + `@Version` para concurrencia de doble capa |
| `carts` | Un carrito activo por usuario |
| `cart_items` | Items del carrito con cantidad y subtotal |
| `orders` | Órdenes completadas |
| `order_items` | Snapshot del producto al momento de la compra |
| `refresh_tokens` | Tokens de renovación con expiración y campo `revoked` |
| `idempotency_keys` | Claves para evitar doble procesamiento en checkout |
| `audit_logs` | Registro de operaciones importantes |
