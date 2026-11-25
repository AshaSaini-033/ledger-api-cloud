# AWS deployment

Terraform provisions a VPC with public and private subnets, internet routing, a public Application Load Balancer, ECS Fargate, ECR, private RDS PostgreSQL, CloudWatch logs, Secrets Manager, IAM task roles and GitHub Actions OIDC.

## Prerequisites
- AWS account and local AWS CLI credentials
- Terraform 1.6+
- Docker
- GitHub repository: AshaSaini-033/ledger-api-cloud

## Deploy infrastructure
1. Copy `terraform.tfvars.example` to `terraform.tfvars`.
2. Set a unique project name, database password and a cryptographically random JWT secret of at least 32 bytes.
3. Run `terraform init`, `terraform fmt -recursive`, `terraform validate` and `terraform plan`.
4. Review the complete plan and expected charges before `terraform apply`.
5. Record outputs: ECR URL, ALB DNS, cluster, service and GitHub OIDC role ARN.

The ECS task obtains the database password and JWT signing key from AWS Secrets Manager. Terraform state still contains sensitive inputs: use an encrypted remote S3 backend with state locking and tightly scoped access before any shared or production use. Do not commit `terraform.tfvars`, state files or credentials.

## GitHub Actions configuration
Add repository variables:
- `AWS_REGION`: same region used by Terraform
- `ECR_REPOSITORY`: ECR repository name (project name)
- `ECS_CLUSTER`: Terraform output `ecs_cluster_name`
- `ECS_SERVICE`: Terraform output `ecs_service_name`

Add repository secret:
- `AWS_DEPLOY_ROLE_ARN`: Terraform output `github_actions_role_arn`

Push to `main` to run CI and the deployment workflow. The deployment workflow publishes both the commit SHA and `latest` image tags and restarts the ECS service.

## Cost and safety
ALB, RDS, ECS, ECR storage, NAT/network traffic and CloudWatch can incur charges. The demo uses one Fargate task and a small RDS instance; it is not production high availability. Destroy resources when finished, after preserving anything needed:
`terraform destroy`
