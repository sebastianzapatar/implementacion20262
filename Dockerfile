# ============================================================
# Dockerfile Multi-Stage Build
# ============================================================
# Este Dockerfile utiliza una estrategia de construcción en múltiples
# etapas (multi-stage build) para generar una imagen final lo más
# liviana y segura posible.
#
# Etapa 1 ("build"): Compila el proyecto con Gradle y JDK 25.
# Etapa 2 ("runtime"): Copia solo el .jar final a una imagen JRE mínima.
# ============================================================

# -------------------------------------------------------
# ETAPA 1: BUILD — Compilación del proyecto
# -------------------------------------------------------
# Usamos una imagen con JDK 25 completo (debe coincidir con
# la versión definida en build.gradle -> languageVersion).
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copiamos primero los archivos de configuración de Gradle
# para aprovechar la caché de capas de Docker (si no cambian,
# Docker no vuelve a descargar las dependencias).
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Damos permisos de ejecución al wrapper de Gradle
RUN chmod +x gradlew

# Descargamos las dependencias SIN compilar el código fuente.
# Esto permite que Docker cachee esta capa pesada.
RUN ./gradlew dependencies --no-daemon

# Ahora sí copiamos todo el código fuente del proyecto
COPY src/ src/

# Compilamos el proyecto y generamos el .jar (sin ejecutar tests)
RUN ./gradlew bootJar --no-daemon -x test

# -------------------------------------------------------
# ETAPA 2: RUNTIME — Imagen final liviana
# -------------------------------------------------------
# Usamos una imagen solo con JRE (sin herramientas de compilación)
# para reducir drásticamente el tamaño de la imagen final.
FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

# Copiamos ÚNICAMENTE el .jar generado en la etapa anterior
COPY --from=build /app/build/libs/*.jar app.jar

# Puerto que expone la aplicación Spring Boot
EXPOSE 8080

# Comando que se ejecuta al iniciar el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]
