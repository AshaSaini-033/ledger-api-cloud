# Ledger API Cloud

A cloud-ready banking ledger REST API built with Java 21 and Spring Boot. It supports user authentication, account management, deposits, withdrawals, transfers, and transaction history, with PostgreSQL persistence and AWS deployment infrastructure.

> **Portfolio / learning project.** This is not a production banking system and must not be used to hold real money or real customer information. It does not implement regulated banking controls, KYC/AML, external payment rails, or a production-grade audit/compliance program.

## What it includes

- **Authentication:** email/password registration and login, BCrypt password hashing, signed JWT bearer tokens, stateless security.
- **Accounts:** create accounts in supported three-letter currencies, list and fetch only accounts owned by the authenticated user, close zero-balance accounts.
- **Ledger operations:** deposits and withdrawals with positive decimal amounts and transaction history.
- **Transfers:** source ownership validation, currency checks, debit and credit ledger rows sharing one transfer ID, all written in one database transaction.
- **Persistence:** PostgreSQL 16, JPA/Hibernate, Flyway schema migration, optimistic version column on accounts.
- **Containers:** multi-stage Java Docker image and Docker Compose for API, PostgreSQL, Prometheus, and Grafana.
- **Cloud infrastructure:** Terraform for VPC/subnets/security groups, private RDS PostgreSQL, ECR, ECS Fargate, ALB, CloudWatch logs, Secrets Manager, IAM, and GitHub Actions OIDC.
- **Delivery / monitoring:** GitHub Actions build-and-test workflow, OIDC-based ECR/ECS deployment workflow, Prometheus metrics, Grafana dashboard.

## Technology stack

| Area | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5 |
| API / Security | Spring Web, Spring Security, JWT, BCrypt, Bean Validation |
| Database | PostgreSQL 16, Spring Data JPA, Hibernate, Flyway |
| API docs / health | springdoc OpenAPI, Spring Boot Actuator |
| Containers | Docker, Docker Compose |
| AWS | VPC, ALB, ECS Fargate, ECR, RDS PostgreSQL, Secrets Manager, CloudWatch, IAM OIDC |
| Infrastructure | Terraform |
| CI/CD | GitHub Actions |
| Observability | Prometheus, Grafana, Micrometer |

## Run locally

Prerequisites: Docker Desktop / Docker Engine and Docker Compose.

1. Clone the repository.
2. Copy `.env.example` to `.env` and use a long random JWT secret.
3. Start the services:

```bash
docker compose up --build
```

Local endpoints:

| Service | URL |
|---|---|
| REST API | http://localhost:8080 |
| Health | http://localhost:8080/actuator/health |
| Prometheus metrics | http://localhost:8080/actuator/prometheus |
| Prometheus UI | http://localhost:9090 |
| Grafana | http://localhost:3000 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

The Compose Grafana configuration is intended for local development only and enables anonymous administrator access. Do not expose it publicly.

### Run tests

Install JDK 21 and Maven, then run:

```bash
mvn -B verify
```

The GitHub Actions CI workflow starts a PostgreSQL service for the Spring application context test.

## API walkthrough

Register a user:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"asha@example.com","password":"change-this-password-123"}'
```

Login and save the JWT:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"asha@example.com","password":"change-this-password-123"}' | jq -r .token)
```

Create an account:

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"currency":"INR"}'
```

Deposit funds (replace the account ID):

```bash
curl -X POST http://localhost:8080/api/accounts/<ACCOUNT_ID>/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":1000.00,"description":"Initial funding"}'
```

Transfer funds:

```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId":"<SOURCE_ACCOUNT_ID>","toAccountId":"<DESTINATION_ACCOUNT_ID>","amount":250.00,"description":"Shared expense"}'
```

Use the same currency for both transfer accounts. A transfer fails as a whole if either balance update or ledger insert fails.

A helper walkthrough is available at `scripts/try-api.sh`.

## API reference

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register and receive a JWT |
| POST | `/api/auth/login` | Authenticate and receive a JWT |
| POST | `/api/accounts` | Create an account |
| GET | `/api/accounts` | List the current user's accounts |
| GET | `/api/accounts/{accountId}` | Fetch an owned account |
| POST | `/api/accounts/{accountId}/close` | Close an empty account |
| POST | `/api/accounts/{accountId}/deposit` | Deposit funds |
| POST | `/api/accounts/{accountId}/withdraw` | Withdraw funds |
| GET | `/api/accounts/{accountId}/transactions` | View latest 100 transactions |
| POST | `/api/transfers` | Transfer between accounts |

All account and money-operation endpoints require `Authorization: Bearer <token>`. Account IDs are ownership-scoped for reads, deposits and withdrawals. A transfer may credit another user's account, but the source must belong to the caller.

## Architecture

```text
Client
  |
  | HTTPS / JWT
  v
Application Load Balancer
  |
  v
ECS Fargate (Spring Boot API)
  |                    |
  | JPA                | Micrometer
  v                    v
RDS PostgreSQL      Prometheus -> Grafana

GitHub Actions -- OIDC --> AWS IAM
       |                    |
       | Docker build       | Push image
       v                    v
      ECR --------------> ECS service update

Terraform provisions the AWS network, data, compute, identity and logging resources.
```

## AWS deployment

The infrastructure code lives in [infra/](infra/). Follow [infra/README.md](infra/README.md) carefully.

The deployment path uses GitHub Actions OIDC rather than long-lived AWS access keys. After applying Terraform, configure repository variables `AWS_REGION`, `ECR_REPOSITORY`, `ECS_CLUSTER`, `ECS_SERVICE` and secret `AWS_DEPLOY_ROLE_ARN` as described in the infra guide.

The ECS task obtains the database password and JWT signing key from AWS Secrets Manager. Terraform state can still contain sensitive values: use an encrypted remote backend and tightly restrict state access. The included example does not create a remote state bucket.

**AWS resources can incur charges.** Inspect `terraform plan` before applying, monitor costs, and run `terraform destroy` when you no longer need the demo environment. This repo contains deployment definitions; a live AWS endpoint is not claimed.

## Design notes and current limitations

- Transfers are executed in a single database transaction and account rows use JPA optimistic versioning to detect concurrent updates. The project does not yet include a bounded cross-transaction retry policy or full idempotency-key replay handling.
- Transaction history is limited to the latest 100 rows; cursor pagination and date filters are future improvements.
- The demo uses one ECS task and a small RDS instance; it is not a high-availability production topology.
- TLS termination, a custom domain, WAF, database proxy, backups/restore drills, secret rotation, rate limiting, refresh-token rotation, and security/load testing are not configured here.
- Review dependency versions, AWS IAM scope, network design, and all Terraform plans before using the code beyond a learning environment.

## License

MIT. See [LICENSE](LICENSE).
