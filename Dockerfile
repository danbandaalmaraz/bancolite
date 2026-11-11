# un solo Dockerfile para los 3 servicios, se elige cual con --build-arg SERVICIO=...

# ---------- etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-21 AS compilacion
ARG SERVICIO
WORKDIR /app

# primero solo los pom, asi Docker reutiliza las dependencias descargadas si no cambian
COPY pom.xml .
COPY servicio-cuentas/pom.xml servicio-cuentas/
COPY servicio-transferencias/pom.xml servicio-transferencias/
COPY servicio-notificaciones/pom.xml servicio-notificaciones/
RUN mvn -q -pl ${SERVICIO} -am dependency:go-offline

# despues el codigo del servicio y se compila
COPY ${SERVICIO}/src ${SERVICIO}/src
RUN mvn -q -pl ${SERVICIO} -am package -DskipTests

# ---------- etapa 2: ejecutar ----------
FROM eclipse-temurin:21-jre
ARG SERVICIO
WORKDIR /app

# no correr como root dentro del contenedor
RUN useradd --system --uid 1001 bancolite
COPY --from=compilacion /app/${SERVICIO}/target/app.jar app.jar
USER bancolite

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]