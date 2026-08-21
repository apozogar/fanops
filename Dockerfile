# ------------------------------------
# FASE 1: BUILD DEL FRONTEND (Angular)
# ------------------------------------
# Usa una imagen Node para construir el frontend
FROM node:20-alpine AS frontend-builder

# El directorio de trabajo es la subcarpeta del frontend
WORKDIR /app/frontend

# Copiamos solo los archivos de configuración y dependencias del frontend
COPY frontend/package*.json ./
COPY frontend/angular.json ./

# Instalar dependencias
RUN npm install

# Copiar el código fuente de Angular
COPY frontend/ ./

# Compilar la aplicación Angular en modo producción
RUN npm run build -- --configuration=production

# ------------------------------------
# FASE 2: BUILD DEL BACKEND (Spring Boot con Maven)
# ------------------------------------
# Usamos la imagen de Maven con Java 21 para construir el proyecto.
FROM maven:3.9-eclipse-temurin-21 AS build

# Establecemos el directorio de trabajo.
WORKDIR /app

# 1. COPIAR EL FRONTEND AL STATIC FOLDER DEL BACKEND
# Reemplaza 'fanops' con el nombre de la carpeta de salida real (p. ej. 'frontend' o el nombre del proyecto)
# Spring Boot busca archivos estáticos en src/main/resources/static/
# ¡IMPORTANTE!: Esta es la línea clave.
COPY --from=frontend-builder /app/frontend/dist/browser/ src/main/resources/static/

# Copiamos solo el pom.xml para cachear las dependencias de Maven.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente del backend.
COPY src ./src

# Construimos el proyecto y generamos el archivo .jar.
RUN mvn package -DskipTests

# ------------------------------------
# FASE 3: IMAGEN FINAL DE PRODUCCIÓN
# ------------------------------------
# Usamos una imagen ligera de Java para ejecutar la aplicación.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# NOTA: Asegúrate de que el puerto 8080 se configure en 'server.port=${PORT:8080}' en Spring Boot.
ENV PORT 8080

# Copiamos el artefacto construido (.jar) desde la etapa anterior.
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto (esto no es estrictamente necesario, pero es informativo)
EXPOSE 8080

# Comando para ejecutar la aplicación.
ENTRYPOINT ["java", "-jar", "app.jar"]