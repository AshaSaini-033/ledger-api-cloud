variable "aws_region" {
  description = "AWS region for the deployment"
  type        = string
  default     = "ap-south-1"
}
variable "project_name" {
  description = "Short unique resource prefix"
  type        = string
  default     = "ledger-api-cloud"
}
variable "github_repository" {
  description = "GitHub owner/repository permitted to assume the deploy role"
  type        = string
}
variable "db_username" {
  description = "RDS master username"
  type        = string
  default     = "ledgeradmin"
}
variable "db_password" {
  description = "RDS master password; keep out of source control"
  type        = string
  sensitive   = true
}
variable "jwt_secret" {
  description = "At least 32 random bytes used to sign access tokens"
  type        = string
  sensitive   = true
}
variable "container_port" {
  description = "Spring Boot HTTP port"
  type        = number
  default     = 8080
}
