# Ledger API Cloud

A cloud-ready banking ledger API built with Java 21, Spring Boot, PostgreSQL, Docker, Terraform and AWS ECS Fargate.

## Features
- JWT authentication and BCrypt password hashing
- Account creation and ownership checks
- Deposits and withdrawals
- Account-to-account transfers
- Transaction history
- PostgreSQL schema migrations with Flyway
- Docker Compose local environment
- AWS infrastructure as code: VPC, ALB, ECS Fargate, ECR and RDS
- GitHub Actions CI and OIDC-based AWS deployment workflow
- Prometheus metrics and Grafana dashboard

## Run locally
```bash
cp .env.example .env
docker compose up --build
```

API: http://localhost:8080
Swagger/OpenAPI is documented in the application setup.

## AWS deployment
See [infra/README.md](infra/README.md). Configure the GitHub Actions repository variables and secrets listed there before enabling deployment. Terraform provisions billable AWS resources; review the plan before applying.

## Note
This is an educational portfolio project, not a production banking system. Do not use real money or real customer data. The cloud deployment is not live until AWS credentials, GitHub OIDC trust, secrets, and infrastructure provisioning are configured.
