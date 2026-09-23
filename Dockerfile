# ---- Build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

# Nécessite un accès réseau à Maven Central au moment du build.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -f pom.xml -DskipTests package \
    || (apt-get update && apt-get install -y maven && mvn -q -DskipTests package)

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

RUN useradd --system --uid 1001 spring
USER spring

COPY --from=build /workspace/target/allo-dakar-backend.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
