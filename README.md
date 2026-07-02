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
┌──────────────────┐         ┌──────────────────┐
│      Chef        │         │      Dish         │
├──────────────────┤         ├──────────────────┤
│ id       (UUID)  │ 1────N  │ id       (UUID)  │
│ nombre   (String)│◄────────│ nombre   (String) │
│ platos   (List)  │         │ descripcion (Str) │
│                  │         │ precio  (Decimal) │
│                  │         │ chef_id (FK-UUID)  │
└──────────────────┘         └──────────────────┘
```

- **Un Chef puede crear muchos Platos** → Relación `@OneToMany`
- **Un Plato pertenece a un solo Chef** → Relación `@ManyToOne`
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

### 4. Manejo Global de Excepciones
Se utiliza `@ControllerAdvice` para capturar excepciones en un solo lugar y devolver respuestas JSON estructuradas al cliente.

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
