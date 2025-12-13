# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build sin usar mount (compatibilidad con entornos sin BuildKit). Si necesitas cacheo,
# habilita BuildKit o añade un volumen local al directorio .m2.
RUN mvn -Dmaven.test.skip=true package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
# Add curl for container healthchecks
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/*.jar /app/app.jar
# You can pass JVM flags via JAVA_OPTS, e.g. -Xms256m -Xmx512m
ENV JAVA_OPTS=""     SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
