# FluxBank Helm Charts

Production-grade Helm charts for deploying FluxBank microservices to Kubernetes.

## Prerequisites

- Kubernetes 1.27+
- Helm 3.12+
- `kubectl` configured against your target cluster
- Secrets applied (see **Required Secrets** below)

## Install the Full Stack

```bash
# 1. Resolve subchart dependencies
helm dependency update ./helm/fluxbank

# 2. Install to production namespace
helm install fluxbank ./helm/fluxbank \
  --namespace fluxbank-production \
  --create-namespace
```

## Upgrade

```bash
helm upgrade fluxbank ./helm/fluxbank \
  --namespace fluxbank-production \
  --values ./helm/fluxbank/values-production.yaml
```

## Staging vs Production Overrides

Override the defaults using environment-specific values files:

```bash
# Staging
helm upgrade --install fluxbank ./helm/fluxbank \
  --namespace fluxbank-staging \
  --create-namespace \
  --set global.imageTag=sha-<commit-sha> \
  --values ./helm/fluxbank/values-staging.yaml

# Production
helm upgrade --install fluxbank ./helm/fluxbank \
  --namespace fluxbank-production \
  --create-namespace \
  --set global.imageTag=sha-<commit-sha> \
  --values ./helm/fluxbank/values-production.yaml
```

## Install a Single Subchart

Each subchart is standalone and can be installed independently:

```bash
helm install auth-service ./helm/fluxbank/charts/auth-service \
  --namespace fluxbank-production \
  --set global.imageRegistry=ghcr.io/fluxbank \
  --set global.imageTag=latest
```

## Required Secrets

Apply these secrets before deploying. Use `k8s/secrets-template.yaml` as a reference — **never commit real values**.

| Secret Name | Namespace | Keys | Used By |
|---|---|---|---|
| `fluxbank-jwt-keys` | fluxbank-production | `private_key.pem`, `public_key.pem` | auth-service |
| `fluxbank-db-credentials` | fluxbank-production | `SPRING_DATASOURCE_PASSWORD` | account-service, auth-service, transaction-service, payment-service, card-service, kyc-service |
| `fluxbank-kafka-credentials` | fluxbank-production | `SPRING_KAFKA_PROPERTIES_SASL_JAAS_CONFIG` | payment-service, notification-service, transaction-service |
| `<service>-secrets` | fluxbank-production | service-specific env vars | each service (optional, loaded via `envFrom`) |

### Generate JWT Keys

```bash
openssl genrsa -out private_key.pem 2048
openssl rsa -in private_key.pem -pubout -out public_key.pem

kubectl create secret generic fluxbank-jwt-keys \
  --namespace fluxbank-production \
  --from-file=private_key.pem=./private_key.pem \
  --from-file=public_key.pem=./public_key.pem
```

### Create DB Credentials

```bash
kubectl create secret generic fluxbank-db-credentials \
  --namespace fluxbank-production \
  --from-literal=SPRING_DATASOURCE_PASSWORD=<your-password>
```

## Chart Structure

```
helm/fluxbank/
├── Chart.yaml                   # Umbrella chart
├── values.yaml                  # Global defaults + per-service replica counts
├── values-staging.yaml          # Staging overrides
├── values-production.yaml       # Production overrides (HPA enabled)
└── charts/
    ├── service-registry/        # Eureka (port 8761)
    ├── config-server/           # Spring Cloud Config (port 8888)
    ├── api-gateway/             # Gateway + JWT + Redis rate limiting (port 8080)
    ├── auth-service/            # JWT RS256 auth (port 8081)
    ├── account-service/         # Bank accounts (port 8082)
    ├── transaction-service/     # CQRS+ES ledger (port 8083)
    ├── payment-service/         # Saga + Outbox (port 8084)
    ├── card-service/            # Virtual/physical cards (port 8085)
    ├── kyc-service/             # KYC lifecycle (port 8086)
    └── notification-service/    # Kafka consumers + email (port 8087)
```

## HPA-Enabled Services

The following services have Horizontal Pod Autoscaling configured in `values-production.yaml`:

| Service | Min Replicas | Max Replicas | CPU Target |
|---|---|---|---|
| api-gateway | 3 | 10 | 70% |
| auth-service | 2 | 6 | 70% |
| account-service | 3 | 8 | 70% |
| payment-service | 2 | 6 | 70% |
