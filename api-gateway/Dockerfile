# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy root pom + common-lib first for layer caching
COPY pom.xml .
COPY common-lib/ common-lib/
COPY api-gateway/ api-gateway/


# Stub missing reactor modules so Maven can resolve the root POM
RUN for m in service-registry config-server auth-service account-service transaction-service payment-service card-service kyc-service notification-service; do \
    mkdir -p "$m" && \
    printf '<project><modelVersion>4.0.0</modelVersion><parent><groupId>com.fluxbank</groupId><artifactId>flux-bank</artifactId><version>0.0.1-SNAPSHOT</version></parent><artifactId>%s</artifactId></project>' "$m" > "$m/pom.xml"; \
  done

# Build only this module (dependencies already cached in .m2)
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -pl api-gateway -am -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: run as non-root
RUN addgroup -S fluxbank && adduser -S fluxbank -G fluxbank
USER fluxbank

COPY --from=builder /workspace/api-gateway/target/*.jar app.jar

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
