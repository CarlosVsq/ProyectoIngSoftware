# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Speed up: use a local maven repo cache layer if needed
RUN --mount=type=cache,target=/root/.m2 mvn -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
# You can pass JVM flags via JAVA_OPTS, e.g. -Xms256m -Xmx512m
ENV JAVA_OPTS=""     SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]