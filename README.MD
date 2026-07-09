# Booking Request API

API REST para la gestión de solicitudes de embarque internacional. Permite crear, consultar, actualizar, cambiar el estado y eliminar booking requests, asociando proveedores e ítems de manera completa.

---

## Tecnologías

| Tecnología | Versión | Descripción |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.5.x | Framework base |
| Spring Data JPA | — | Persistencia y acceso a datos |
| Spring Web | — | API REST |
| Spring Validation | — | Validación de entrada |
| PostgreSQL | 15+ | Base de datos relacional |
| Hibernate | — | ORM (incluido con JPA) |
| Lombok | — | Reducción de código boilerplate |
| SpringDoc OpenAPI | 2.8.9 | Documentación Swagger UI |
| JUnit 5 | — | Framework de pruebas unitarias |
| Mockito | — | Mocking de dependencias en tests |
| AssertJ | — | Aserciones expresivas en tests |
| Maven | 3.9.x | Gestión de dependencias y build |

---

## Descripción del proyecto

El proyecto implementa una **API REST de solicitudes de embarque** usando **arquitectura hexagonal** (también llamada Ports & Adapters), lo que garantiza una separación clara entre la lógica de negocio y los detalles tecnológicos.

### Estructura de capas

```
domain/          → Modelos, enums, excepciones y puertos (contratos)
application/     → Servicios que orquestan la lógica de negocio
infrastructure/  → Controladores REST, entidades JPA, repositorios y mappers
```

### Modelo de datos

```
Supplier (Proveedor)
  └── 1:N BookingRequest (Solicitud de embarque)
              └── 1:N BookingItem (Ítems del embarque)
```

### Estados y transiciones

```
DRAFT ──→ CONFIRMED
DRAFT ──→ CANCELLED
CONFIRMED ──→ CANCELLED
CANCELLED  (estado terminal — sin más transiciones)
```

---

## Requisitos previos

- **Java 17** o superior
- **Maven 3.9+** (o usar el wrapper incluido `mvnw`)
- **PostgreSQL 15+** corriendo localmente
- Puerto **5432** disponible para PostgreSQL
- Puerto **8080** disponible para la aplicación

---

## Configuración de la base de datos

### 1. Crear la base de datos

Conéctate a PostgreSQL y ejecuta:

```sql
CREATE DATABASE postgres;
```

> Si ya existe la base de datos `postgres` (por defecto en instalaciones locales), puedes omitir este paso.

### 2. Ejecutar el schema

```bash
psql -U postgres -d postgres -f sql/schema.sql
```

Esto crea las tablas `supplier`, `booking_request` y `booking_item` con sus índices y constraints.

### 3. Cargar datos de prueba (opcional)

```bash
psql -U postgres -d postgres -f sql/data.sql
```

Carga 3 proveedores y 4 booking requests en distintos estados (DRAFT, CONFIRMED, CANCELLED) con sus ítems correspondientes.

### 4. Credenciales por defecto

La configuración en `application.properties` usa:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=Pro2588
```

Si tus credenciales son distintas, edita `src/main/resources/application.properties` antes de ejecutar.

---

## Cómo ejecutar el proyecto

### Opción 1 — Maven Wrapper (recomendado)

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

### Opción 2 — Maven instalado

```bash
mvn spring-boot:run
```

### Opción 3 — Generar JAR y ejecutar

```bash
.\mvnw.cmd package -DskipTests
java -jar target/booking-0.0.1-SNAPSHOT.jar
```

Una vez iniciado, la aplicación estará disponible en:

```
http://localhost:8080
```

---

## Ejecutar las pruebas unitarias

```bash
# Todos los tests
.\mvnw.cmd test

# Solo los tests de la capa de servicio
.\mvnw.cmd test -Dtest=BookingServiceTest

# Solo los tests del modelo de dominio
.\mvnw.cmd test -Dtest=BookingRequestStateTest

# Solo los tests de BookingItem
.\mvnw.cmd test -Dtest=BookingItemTest

# Los tres a la vez
.\mvnw.cmd test -Dtest=BookingServiceTest,BookingRequestStateTest,BookingItemTest
```

Los reportes quedan en `target/surefire-reports/`.

---

## Documentación con Swagger UI

Con la aplicación corriendo, accede a:

| URL | Descripción |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interfaz visual interactiva |
| `http://localhost:8080/v3/api-docs` | Especificación OpenAPI en JSON |
| `http://localhost:8080/v3/api-docs.yaml` | Especificación OpenAPI en YAML |

### Vista de la API en Swagger

La documentación muestra el tag **Booking Requests** con los 6 endpoints disponibles:

```
GET    /api/bookings              → Listar con filtros opcionales
POST   /api/bookings              → Crear booking request
GET    /api/bookings/{id}         → Obtener por ID
PATCH  /api/bookings/{id}         → Actualizar campos (solo DRAFT)
PATCH  /api/bookings/{id}/status  → Cambiar estado
DELETE /api/bookings/{id}         → Soft delete
```

Desde Swagger UI puedes ejecutar cada endpoint directamente:

1. Haz clic en el endpoint que quieres probar
2. Haz clic en **Try it out**
3. Completa los parámetros o el body
4. Haz clic en **Execute**
5. Observa la respuesta en la sección **Responses**

---

## Referencia de endpoints

### GET /api/bookings — Listar con filtros

Todos los parámetros son opcionales:

| Parámetro | Tipo | Ejemplo | Descripción |
|---|---|---|---|
| `taxId` | String | `TAX-88000222-2` | RUT o Tax ID del proveedor |
| `status` | Enum | `DRAFT` | DRAFT, CONFIRMED, CANCELLED |
| `freightMode` | Enum | `SEA` | AIR, SEA, ROAD |
| `dateFrom` | Date | `2026-01-01` | issueDate desde (yyyy-MM-dd) |
| `dateTo` | Date | `2026-12-31` | issueDate hasta (yyyy-MM-dd) |
| `bookingCode` | String | `BOOK-TEST-0001` | Búsqueda exacta por código |

```
GET http://localhost:8080/api/bookings
GET http://localhost:8080/api/bookings?status=DRAFT
GET http://localhost:8080/api/bookings?taxId=TAX-88000222-2&freightMode=SEA
GET http://localhost:8080/api/bookings?dateFrom=2026-01-01&dateTo=2026-06-30
```

### POST /api/bookings — Crear booking

```json
{
  "bookingCode": "BOOK-NEW-0005",
  "issueDate": "2026-07-01",
  "expirationDate": "2026-12-31",
  "currency": "USD",
  "incotermCode": "FOB",
  "freightMode": "SEA",
  "originCountry": "China",
  "destinationCountry": "Chile",
  "fobValue": 15000.00,
  "supplierTaxId": "TAX-88000222-2",
  "items": [
    {
      "sku": "SKU-PROD-001",
      "description": "Notebook empresarial",
      "quantity": 10,
      "unitPrice": 850.00
    }
  ]
}
```

> `totalAmount` de cada ítem **no se envía** — se calcula automáticamente como `quantity × unitPrice`.

### PATCH /api/bookings/{id} — Actualizar (solo DRAFT)

Solo se modifican los campos presentes en el body (los `null` se ignoran):

```json
{
  "expirationDate": "2027-03-31",
  "fobValue": 18000.00,
  "currency": "EUR"
}
```

### PATCH /api/bookings/{id}/status — Cambiar estado

```json
{
  "status": "CONFIRMED"
}
```

### DELETE /api/bookings/{id} — Soft delete

No requiere body. Retorna `204 No Content`.

---

## Cómo ejecutar con Postman

### Importar la colección

1. Abre Postman
2. Haz clic en **Import**
3. Si existe un archivo en la carpeta `postman/`, arrástralo a la ventana de importación

> Si la carpeta `postman/` está vacía, puedes crear la colección manualmente con los ejemplos de esta sección.

### Configurar el entorno

Crea un entorno en Postman con la siguiente variable:

| Variable | Valor |
|---|---|
| `base_url` | `http://localhost:8080` |

### Flujo de prueba recomendado

Sigue este orden para probar el ciclo completo de un booking:

**1. Listar proveedores disponibles (verificar datos de prueba)**
```
GET {{base_url}}/api/bookings?status=DRAFT
```

**2. Crear un nuevo booking**
```
POST {{base_url}}/api/bookings
Content-Type: application/json

{
  "bookingCode": "BOOK-POSTMAN-001",
  "issueDate": "2026-07-10",
  "expirationDate": "2026-12-31",
  "currency": "USD",
  "incotermCode": "FOB",
  "freightMode": "SEA",
  "originCountry": "China",
  "destinationCountry": "Chile",
  "fobValue": 20000.00,
  "supplierTaxId": "TAX-88000222-2",
  "items": [
    {
      "sku": "SKU-001",
      "description": "Producto de prueba",
      "quantity": 50,
      "unitPrice": 200.00
    }
  ]
}
```

Guarda el `id` retornado para los siguientes pasos.

**3. Obtener el booking creado**
```
GET {{base_url}}/api/bookings/{id}
```

**4. Actualizar campos (solo DRAFT)**
```
PATCH {{base_url}}/api/bookings/{id}
Content-Type: application/json

{
  "fobValue": 25000.00,
  "currency": "EUR"
}
```

**5. Confirmar el booking**
```
PATCH {{base_url}}/api/bookings/{id}/status
Content-Type: application/json

{
  "status": "CONFIRMED"
}
```

**6. Cancelar el booking**
```
PATCH {{base_url}}/api/bookings/{id}/status
Content-Type: application/json

{
  "status": "CANCELLED"
}
```

**7. Eliminar (soft delete)**
```
DELETE {{base_url}}/api/bookings/{id}
```

---

## Reglas de negocio

| # | Regla |
|---|---|
| 1 | `totalAmount` de cada ítem = `quantity × unitPrice` (calculado en backend) |
| 2 | El booking debe tener al menos un ítem |
| 3 | El `taxId` del proveedor es obligatorio y debe existir en la base de datos |
| 4 | `issueDate` debe ser menor o igual a `expirationDate` |
| 5 | `currency` e `incotermCode` son obligatorios |
| 6 | Solo se puede actualizar un booking en estado `DRAFT` |
| 7 | Solo se puede eliminar un booking en estado `DRAFT` o `CANCELLED` |
| 8 | `CANCELLED` es estado terminal — no admite más transiciones |

---

## Códigos de respuesta HTTP

| Código | Descripción |
|---|---|
| `200 OK` | Operación exitosa |
| `201 Created` | Booking creado exitosamente |
| `204 No Content` | Soft delete realizado |
| `400 Bad Request` | Error de validación en los datos de entrada |
| `404 Not Found` | Booking o proveedor no encontrado |
| `422 Unprocessable Entity` | Regla de negocio no cumplida |
