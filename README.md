# 🍳 API de Gestión de Restaurante — Repaso Spring Boot

API REST desarrollada con **Spring Boot 4** para la gestión de **Chefs** y sus **Platos**. Este proyecto fue creado como material de repaso para comprender los conceptos fundamentales de desarrollo backend con Java y Spring.

---

## 📋 Tabla de Contenidos

- [Tecnologías](#-tecnologías)
- [Arquitectura del Proyecto](#-arquitectura-del-proyecto)
- [Estructura de Carpetas](#-estructura-de-carpetas)
- [Modelo de Datos](#-modelo-de-datos)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Configuración y Ejecución](#-configuración-y-ejecución)
- [Docker / Podman](#-docker--podman)
- [Documentación Swagger](#-documentación-swagger)
- [Conceptos Clave del Proyecto](#-conceptos-clave-del-proyecto)

---

## 🛠 Tecnologías

| Tecnología | Versión | Descripción |
|---|---|---|
| Java | 25 | Lenguaje de programación |
| Spring Boot | 4.1.0 | Framework principal |
| Spring Data JPA | — | Acceso a datos con Hibernate |
| PostgreSQL | 15 | Base de datos relacional |
| Lombok | — | Reducción de código boilerplate |
| SpringDoc OpenAPI | 3.0.3 | Documentación Swagger |
| Gradle | 9.5.1 | Gestor de dependencias y build |
| Docker / Podman | — | Contenedorización |

---

## 🏗 Arquitectura del Proyecto

Este proyecto utiliza una arquitectura de **Vertical Slicing** (corte vertical por funcionalidad), donde cada entidad del dominio tiene su propia carpeta con todas sus capas:

```
┌─────────────────────────────────────────────────┐
│                  Controller                     │  ← Recibe peticiones HTTP
├─────────────────────────────────────────────────┤
│                   Service                       │  ← Lógica de negocio y validaciones
├─────────────────────────────────────────────────┤
│                  Repository                     │  ← Acceso a la base de datos
├─────────────────────────────────────────────────┤
│                   Entity                        │  ← Representación de la tabla en BD
├─────────────────────────────────────────────────┤
│               DTO (Request/Response)            │  ← Objetos de transferencia de datos
├─────────────────────────────────────────────────┤
│                   Mapper                        │  ← Conversión entre Entity ↔ DTO
└─────────────────────────────────────────────────┘
```

---

## 📂 Estructura de Carpetas

```
src/main/java/com/nomelestar/repaso/
│
├── RepasoApplication.java            # Clase principal (punto de entrada)
│
├── chef/                             # Feature: Chef
│   ├── controller/
│   │   └── ChefController.java       # Endpoints REST del Chef
│   ├── dto/
│   │   ├── ChefRequest.java          # DTO de entrada (record)
│   │   └── ChefResponse.java         # DTO de salida (record)
│   ├── entity/
│   │   └── Chef.java                 # Entidad JPA (tabla "chefs")
│   ├── mapper/
│   │   └── ChefMapper.java           # Conversión Entity ↔ DTO
│   ├── repository/
│   │   └── ChefRepository.java       # Repositorio JPA + Queries personalizados
│   └── service/
│       └── ChefService.java          # Lógica de negocio
│
├── dish/                             # Feature: Plato (Dish)
│   ├── controller/
│   │   └── DishController.java
│   ├── dto/
│   │   ├── DishRequest.java
│   │   └── DishResponse.java
│   ├── entity/
│   │   └── Dish.java                 # Entidad JPA con @ManyToOne hacia Chef
│   ├── mapper/
│   │   └── DishMapper.java
│   ├── repository/
│   │   └── DishRepository.java
│   └── service/
│       └── DishService.java
│
├── client/                           # Feature: Cliente
│   ├── controller/
│   │   └── ClientController.java
│   ├── dto/
│   │   ├── ClientRequest.java
│   │   └── ClientResponse.java
│   ├── entity/
│   │   └── Client.java               # Entidad JPA con @ManyToMany hacia Dish
│   ├── mapper/
│   │   └── ClientMapper.java
│   ├── repository/
│   │   └── ClientRepository.java
│   └── service/
│       └── ClientService.java
│
└── common/                           # Código compartido entre features
    ├── config/
    │   └── OpenApiConfig.java        # Configuración de Swagger/OpenAPI
    └── exception/
        ├── GlobalExceptionHandler.java   # @ControllerAdvice (manejo global de errores)
        ├── ResourceNotFoundException.java # Excepción 404
        ├── BadRequestException.java       # Excepción 400
        └── ErrorResponse.java             # DTO para errores estructurados
```

---

## 🗄 Modelo de Datos

### Relación entre Entidades

```
┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐
│      Chef        │         │      Dish         │         │     Client       │
├──────────────────┤         ├──────────────────┤         ├──────────────────┤
│ id       (UUID)  │ 1────N  │ id       (UUID)  │ N────M  │ id       (UUID)  │
│ nombre   (String)│◄────────│ nombre   (String) │───────►│ nombre   (String)│
│ platos   (List)  │         │ descripcion (Str) │         │ email    (String)│
│                  │         │ precio  (Decimal) │         │ platos   (List)  │
│                  │         │ chef_id (FK-UUID)  │         │                  │
└──────────────────┘         └──────────────────┘         └──────────────────┘
```

- **Un Chef puede crear muchos Platos** → Relación `@OneToMany`
- **Un Plato pertenece a un solo Chef** → Relación `@ManyToOne`
- **Un Cliente puede consumir muchos Platos y un Plato puede ser consumido por muchos Clientes** → Relación `@ManyToMany` (con tabla intermedia `dish_clients`)
- Si se elimina un Chef, se eliminan todos sus platos (`CascadeType.ALL`)

---

## 🌐 Endpoints de la API

### Chefs (`/api/chefs`)

| Método | URL | Descripción |
|---|---|---|
| `POST` | `/api/chefs` | Crear un nuevo chef |
| `GET` | `/api/chefs` | Obtener todos los chefs |
| `GET` | `/api/chefs/{id}` | Obtener un chef por ID |
| `PUT` | `/api/chefs/{id}` | Actualizar un chef |
| `DELETE` | `/api/chefs/{id}` | Eliminar un chef (y sus platos) |

**Ejemplo de Request (POST):**
```json
{
  "nombre": "Gordon Ramsay"
}
```

**Ejemplo de Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Gordon Ramsay",
  "platos": ["Beef Wellington", "Risotto"]
}
```

### Platos (`/api/dishes`)

| Método | URL | Descripción |
|---|---|---|
| `POST` | `/api/dishes` | Crear un nuevo plato |
| `GET` | `/api/dishes` | Obtener todos los platos |
| `GET` | `/api/dishes/{id}` | Obtener un plato por ID |
| `PUT` | `/api/dishes/{id}` | Actualizar un plato |
| `DELETE` | `/api/dishes/{id}` | Eliminar un plato |

**Ejemplo de Request (POST):**
```json
{
  "nombre": "Beef Wellington",
  "descripcion": "Filete de res envuelto en hojaldre con paté y champiñones",
  "precio": 45.99,
  "chefId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Ejemplo de Response:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nombre": "Beef Wellington",
  "descripcion": "Filete de res envuelto en hojaldre con paté y champiñones",
  "precio": 45.99,
  "chefId": "550e8400-e29b-41d4-a716-446655440000",
  "nombreChef": "Gordon Ramsay"
}
```

### Clientes (`/api/clients`)

| Método | URL | Descripción |
|---|---|---|
| `POST` | `/api/clients` | Crear un nuevo cliente |
| `GET` | `/api/clients` | Obtener todos los clientes |
| `GET` | `/api/clients/{id}` | Obtener un cliente por ID |
| `PUT` | `/api/clients/{id}` | Actualizar un cliente |
| `DELETE` | `/api/clients/{id}` | Eliminar un cliente |
| `POST` | `/api/clients/{clientId}/dishes/{dishId}` | Registrar el consumo de un plato por un cliente |
| `GET` | `/api/clients/stats/chef-mas-vendido` | Obtener el chef con más platos vendidos |
| `GET` | `/api/clients/{clientId}/chefs/{chefId}/platos` | Obtener platos preferidos de un cliente por chef |

**Ejemplo de Request (POST):**
```json
{
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com"
}
```

**Ejemplo de Response:**
```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "platosConsumidos": []
}
```

### Manejo de Errores

Todas las respuestas de error siguen este formato:

```json
{
  "message": "No se encontró ningún Chef con el ID: ...",
  "status": 404,
  "timestamp": "2026-07-02T12:00:00.000000"
}
```

| Código | Excepción | Cuándo ocurre |
|---|---|---|
| `400` | `BadRequestException` | Datos inválidos (nombre vacío, precio negativo, etc.) |
| `404` | `ResourceNotFoundException` | El recurso solicitado no existe |
| `500` | `Exception` | Error interno del servidor |

---

## ⚙ Configuración y Ejecución

### Prerrequisitos

- Java 25+ instalado
- Docker **o** Podman instalado (con soporte para compose)
- (Opcional) IntelliJ IDEA o cualquier IDE con soporte para Gradle

### Opción 1: Ejecución Local (desarrollo)

Levantar solo la base de datos y ejecutar la app con Gradle:

**Con Docker:**
```bash
docker compose up db -d
./gradlew bootRun
```

**Con Podman:**
```bash
podman compose up db -d
./gradlew bootRun
```

La aplicación estará disponible en `http://localhost:8080`

### Perfiles de configuración

La configuración está separada por ambiente. **El nombre del archivo es literal** —
Spring lo busca exactamente así:

```
application-<nombre-del-perfil>.yml
            ↑ guion, NO guion bajo ni punto. Distingue mayúsculas.
```

| Archivo | Perfil | Para qué sirve |
|---|---|---|
| `application.yml` | *(siempre)* | Lo común a todos: nombre de la app, `open-in-view`, puerto |
| `application-dev.yml` | `dev` | PostgreSQL en localhost, logs SQL, `ddl-auto: update` |
| `application-docker.yml` | `docker` | Host `db` en la red de Docker, credenciales por variables |
| `application-prod.yml` | `prod` | `ddl-auto: validate`, sin log de SQL, logs en `info` |
| `application-test.yml` | `test` | H2 en memoria (está en `src/test/resources`) |

`application.yml` **siempre** se carga; el archivo del perfil se aplica encima y
pisa lo que repita. Si el nombre no coincide, Spring **no avisa**: ignora el
archivo y arranca con la configuración base.

**Cómo activar un perfil** (de menor a mayor prioridad):

```bash
# 1. Por defecto, declarado en application.yml -> dev
./gradlew bootRun

# 2. Variable de entorno (es lo que usa compose.yml)
SPRING_PROFILES_ACTIVE=prod java -jar app.jar

# 3. Argumento del programa
java -jar app.jar --spring.profiles.active=prod

# 4. Propiedad de la JVM
java -Dspring.profiles.active=prod -jar app.jar
```

En los tests se activa con la anotación:

```java
@SpringBootTest
@ActiveProfiles("test")
class MiTest { … }
```

> ⚠️ **Cuidado con el nombre de las variables de entorno.** En los perfiles se usa
> `${DB_URL}` y no `${SPRING_DATASOURCE_URL}`. Spring normaliza este último a la
> propiedad `spring.datasource.url`, que es justo la que se está definiendo: queda
> una autorreferencia, el placeholder no se resuelve y al driver le llega el texto
> literal `${SPRING_DATASOURCE_URL}` con un error confuso
> (*"Driver claims to not accept jdbcUrl"*).

### Opción 2: Todo con Docker / Podman (producción)

Construir y levantar todos los servicios (base de datos + aplicación):

**Con Docker:**
```bash
# Construir y levantar
docker compose up --build -d

# Ver los logs de la app
docker compose logs -f app

# Detener todo
docker compose down

# Detener y eliminar datos de la BD
docker compose down -v
```

**Con Podman:**
```bash
# Construir y levantar
podman compose up --build -d

# Ver los logs de la app
podman compose logs -f app

# Detener todo
podman compose down

# Detener y eliminar datos de la BD
podman compose down -v
```

> **Nota sobre Podman:** Podman es una alternativa a Docker que no requiere un daemon en segundo plano (es _daemonless_). Los comandos son prácticamente idénticos. Si usas `podman-compose` en lugar del plugin integrado, reemplaza `podman compose` por `podman-compose`.

---

## 🐳 Docker / Podman

### Dockerfile Multi-Stage

El proyecto utiliza un **Dockerfile con construcción multi-etapa** para optimizar la imagen final:

```
┌─────────────────────────────────────┐
│  ETAPA 1: BUILD (JDK 25)           │
│  - Copia archivos de Gradle        │
│  - Descarga dependencias           │
│  - Compila el proyecto             │
│  - Genera el .jar                  │
│       (imagen pesada ~800MB)       │
└──────────────┬──────────────────────┘
               │ Solo copia el .jar
               ▼
┌─────────────────────────────────────┐
│  ETAPA 2: RUNTIME (JRE 25)         │
│  - Imagen mínima sin JDK           │
│  - Solo contiene el .jar           │
│  - Ejecuta la aplicación           │
│       (imagen liviana ~300MB)      │
└─────────────────────────────────────┘
```

**¿Por qué multi-stage?**
- La imagen final es mucho más **pequeña** (solo JRE, no JDK completo).
- Es más **segura** (no incluye herramientas de compilación).
- Aprovecha la **caché de capas de Docker** para builds más rápidos.

### Docker Compose / Podman Compose

El archivo `compose.yml` es compatible tanto con Docker como con Podman y orquesta dos servicios:

| Servicio | Imagen | Puerto | Descripción |
|---|---|---|---|
| `db` | `postgres:15-alpine` | `5432` | Base de datos PostgreSQL |
| `app` | Build desde `Dockerfile` | `8080` | Aplicación Spring Boot |

Características clave:
- **`healthcheck`** en PostgreSQL: la app no inicia hasta que la BD esté lista.
- **`depends_on` con `condition`**: garantiza el orden de arranque.
- **Variables de entorno**: sobreescriben el `application.yml` para apuntar al contenedor de la BD (`db` en vez de `localhost`).
- **Volumen persistente**: los datos no se pierden al reiniciar los contenedores.

### Docker vs Podman — ¿Cuál usar?

| Característica | Docker | Podman |
|---|---|---|
| Daemon | Sí (dockerd) | No (daemonless) |
| Requiere root | Sí (por defecto) | No (rootless) |
| CLI compatible | — | Sí (mismos comandos) |
| Compose | `docker compose` | `podman compose` |
| OCI compliant | Sí | Sí |

Ambas herramientas usan el mismo formato de `Dockerfile` y `compose.yml`, por lo que **este proyecto funciona con cualquiera de las dos sin cambios**.

---

## 📖 Documentación Swagger

Una vez la aplicación esté corriendo, puedes acceder a la documentación interactiva:

| Recurso | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

Desde la interfaz de Swagger puedes:
- Ver todos los endpoints organizados por categorías (Chefs, Dishes)
- Probar cada endpoint directamente con el botón **"Try it out"**
- Ver los modelos de datos (DTOs) que acepta y devuelve cada endpoint

---

## 📚 Conceptos Clave del Proyecto

### 1. DTOs con Records
Los DTOs se implementan como `record` de Java, que son clases inmutables con getters automáticos:
```java
public record ChefRequest(String nombre) {}
```

### 2. Mappers Manuales
Se usan clases utilitarias con métodos estáticos para convertir entre Entity y DTO, evitando exponer la entidad de base de datos al cliente.

### 3. Consultas Personalizadas (@Query)
El `ChefRepository` muestra tres formas de hacer consultas:
- **Query Methods**: Spring genera la SQL desde el nombre del método (`findByNombre`).
- **JPQL**: Consulta orientada a objetos usando los nombres de las clases Java.
- **SQL Nativo**: Consulta SQL tradicional usando los nombres reales de las tablas.

### 3.1 Consultas sobre la relación ManyToMany: los dos enfoques

La relación `Client <-> Dish` (tabla intermedia `dish_clients`) sirve para comparar
las dos maneras de resolver un JOIN. Ambas están probadas en
`src/test/java/com/nomelestar/repaso/query/ManyToManyQueryTest.java`.

**a) CON `@Query` — consulta compleja** (`ClientRepository.rankingPlatosMasConsumidos`)

Ranking de platos más consumidos. Combina doble JOIN (por la ManyToMany y por el chef),
`WHERE`, `GROUP BY`, `HAVING`, `COUNT(DISTINCT)`, `SUM` y una proyección a DTO:

```java
@Query("""
        SELECT new com.nomelestar.repaso.dish.dto.DishPopularityResponse(
                   d.id, d.nombre, ch.nombre, d.precio,
                   COUNT(DISTINCT cl.id), SUM(d.precio))
        FROM Dish d
             JOIN d.clientes cl
             JOIN d.chef ch
        WHERE d.precio >= :precioMinimo
        GROUP BY d.id, d.nombre, ch.nombre, d.precio
        HAVING COUNT(DISTINCT cl.id) >= :minimoClientes
        ORDER BY COUNT(DISTINCT cl.id) DESC, d.precio DESC
        """)
List<DishPopularityResponse> rankingPlatosMasConsumidos(...);
```

`GET /api/clients/stats/ranking-platos?precioMinimo=0&minimoClientes=1`

**b) SIN `@Query` — métodos derivados** (`DishRepository`)

Spring Data arma el JOIN con la tabla intermedia leyendo el nombre del método.
El guion bajo `_` marca dónde termina una propiedad y empieza la siguiente:

```java
List<Dish> findByClientes_Id(UUID clientId);
List<Dish> findByClientes_IdAndChef_IdOrderByPrecioDesc(UUID clientId, UUID chefId);
List<Dish> findByClientes_EmailIgnoreCaseAndPrecioGreaterThanEqual(String email, BigDecimal precioMinimo);
long       countByClientes_Id(UUID clientId);
boolean    existsByIdAndClientes_Id(UUID dishId, UUID clientId);

@EntityGraph(attributePaths = {"chef"})   // JOIN FETCH sin escribir @Query
List<Dish> findByClientes_Nombre(String nombreCliente);
```

`GET /api/dishes/by-client/{clientId}`
`GET /api/dishes/by-client/{clientId}/chef/{chefId}`
`GET /api/dishes/by-client/{clientId}/count`

> `findByClientes_IdAndChef_IdOrderByPrecioDesc` devuelve exactamente lo mismo que
> `ClientRepository.findPlatosDeClientePorChef`, que usa `@Query`. Sirven para
> comparar los dos estilos lado a lado.

**¿Cuál usar?** El método derivado gana cuando el filtro es simple: no hay JPQL que
mantener y el compilador valida el nombre al arrancar. El `@Query` gana cuando hay
agregados (`GROUP BY`, `HAVING`), proyecciones a DTO o el nombre del método derivado
quedaría impronunciable.

### 4. Manejo Global de Excepciones
Se utiliza `@RestControllerAdvice` para capturar excepciones en un solo lugar y devolver
respuestas JSON estructuradas al cliente:

| Situación | Código |
|---|---|
| Recurso inexistente (`ResourceNotFoundException`) | 404 |
| Falla de validación `@Valid` (incluye mapa campo → error) | 400 |
| UUID mal formado en la URL / JSON inválido | 400 |
| Email duplicado u otra violación de constraint | 409 |
| Error inesperado | 500 (mensaje genérico; el detalle va al log) |

### 5. Relaciones JPA
- `@OneToMany` en Chef → Lista de platos
- `@ManyToOne` en Dish → Referencia al chef creador
- `CascadeType.ALL` → Operaciones en cascada (al borrar un chef se borran sus platos)

### 6. Dockerfile Multi-Stage
- Etapa de **build**: JDK completo para compilar.
- Etapa de **runtime**: Solo JRE para ejecutar. Imagen mucho más liviana y segura.

### 7. Docker Compose
- Orquestación de múltiples contenedores con un solo archivo.
- `healthcheck` para controlar el orden de arranque.
- Variables de entorno para sobreescribir configuración de Spring Boot.

---

## 👨‍💻 Autor

Proyecto de repaso — Implementación 2026-2
