# syntax=docker/dockerfile:1

# ------------------------------------
# FASE 1: BUILD DEL FRONTEND (Angular)
# ------------------------------------
FROM node:22-alpine AS frontend-builder
WORKDIR /app/frontend

# Solo el manifiesto primero: mientras no cambien las dependencias, esta capa se reutiliza.
COPY frontend/package.json frontend/package-lock.json ./
# 'npm ci' respeta el lockfile y es reproducible, a diferencia de 'npm install'.
RUN --mount=type=cache,target=/root/.npm npm ci

COPY frontend/ ./
RUN npm run build -- --configuration=production

# ------------------------------------
# FASE 2: BUILD DEL BACKEND (Spring Boot con Maven)
# ------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Las dependencias primero, para que un cambio en el código fuente no vuelva a bajarlas.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

# El frontend compilado se sirve desde dentro del jar (ver SpaWebConfig).
COPY --from=frontend-builder /app/frontend/dist/browser/ src/main/resources/static/
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B package -DskipTests

# ------------------------------------
# FASE 3: IMAGEN FINAL DE PRODUCCIÓN
# ------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ENV PORT=8080
# MaxRAMPercentage: el contenedor tiene 1 GB y la JVM necesita dejar sitio para metaspace,
# hilos y buffers fuera del heap. TieredStopAtLevel=1 recorta el arranque a costa de algo
# de rendimiento pico, un intercambio que compensa en una app con esta carga.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Duser.timezone=Europe/Madrid -Dfile.encoding=UTF-8"

COPY --from=build /app/target/*.jar app.jar

# Se desempaqueta el jar (Spring Boot 3.3+, jarmode 'tools'): con las clases y las
# librerías sueltas en el sistema de ficheros, la JVM puede usar un archivo CDS, que es
# lo que recorta el arranque.
RUN java -Djarmode=tools -jar app.jar extract --destination /app/extracted \
    && rm app.jar

# Entrenamiento de CDS: se arranca la aplicación hasta el refresco del contexto y se sale,
# guardando las clases cargadas en app.jsa. Se desactiva todo lo que necesitaría una base
# de datos (Liquibase, DDL de Hibernate y la lectura de metadatos JDBC), porque durante el
# build no hay ninguna a la que conectarse. El seeder del superadmin es un
# CommandLineRunner, así que con 'spring.context.exit=onRefresh' ni se ejecuta.
#
# Si el entrenamiento falla, el build continúa: en tiempo de ejecución un archivo CDS que
# no exista o no valga solo produce un aviso de la JVM (-Xshare:auto), no un error.
# El 'timeout' es un cinturón de seguridad: si el entrenamiento se quedara esperando algo
# (una conexión, un recurso), el build no se puede quedar colgado por ello.
RUN timeout 420 java $JAVA_OPTS -XX:ArchiveClassesAtExit=/app/app.jsa \
      -Dspring.context.exit=onRefresh \
      -Dspring.liquibase.enabled=false \
      -Dspring.jpa.hibernate.ddl-auto=none \
      -Dspring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false \
      -jar /app/extracted/app.jar > /tmp/cds.log 2>&1 \
    || { echo "AVISO: el entrenamiento de CDS falló, se continúa sin archivo CDS"; tail -30 /tmp/cds.log; }

# Sin privilegios: si alguna vez se cuela una ejecución de código, que no sea como root.
RUN addgroup -S fanops && adduser -S fanops -G fanops && chown -R fanops:fanops /app
USER fanops

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -XX:SharedArchiveFile=/app/app.jsa -jar /app/extracted/app.jar"]
