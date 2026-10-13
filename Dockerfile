# syntax=docker/dockerfile:1

# =========================================================
# Stage 1 - BUILD
# Uses Maven + JDK 17 (matches <java.version>17</java.version> in pom.xml)
# =========================================================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only the POM first so Maven dependencies are cached in their own layer.
# They are re-downloaded only when pom.xml changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copy the application source and build the executable Spring Boot jar.
# The jar name is NOT assumed: the target folder is copied on the next stage.
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# =========================================================
# Stage 2 - RUNTIME
# Small JRE-only image (no JDK/Maven/source code)
# =========================================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Run the app as a non-root user.
RUN useradd --system --uid 1001 spring
USER spring

# Copy the single fat jar produced by Spring Boot and give it a stable name.
# This avoids hardcoding the versioned jar name (canteengo-1.0.0.jar).
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
