# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy root pom + common-lib first for layer caching
COPY pom.xml .
COPY common-lib/ common-lib/
COPY card-service/ card-service/

# Build only this module (dependencies already cached in .m2)
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -pl card-service -am -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: run as non-root
RUN addgroup -S fluxbank && adduser -S fluxbank -G fluxbank
USER fluxbank

COPY --from=builder /workspace/card-service/target/*.jar app.jar

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8085
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
