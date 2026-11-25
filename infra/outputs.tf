output "ecr_repository_url" {
  description = "ECR registry/repository URL"
  value       = aws_ecr_repository.api.repository_url
}
output "load_balancer_dns" {
  description = "Public HTTP address of the API load balancer"
  value       = aws_lb.api.dns_name
}
output "rds_endpoint" {
  description = "Private RDS PostgreSQL endpoint"
  value       = aws_db_instance.ledger.address
}
output "ecs_cluster_name" {
  value = aws_ecs_cluster.api.name
}
output "ecs_service_name" {
  value = aws_ecs_service.api.name
}
output "github_actions_role_arn" {
  description = "Configure as AWS_DEPLOY_ROLE_ARN in GitHub Actions secrets"
  value       = aws_iam_role.github_actions.arn
}
