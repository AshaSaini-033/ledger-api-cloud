resource "aws_db_subnet_group" "ledger" {
  name       = "${var.project_name}-db-subnets"
  subnet_ids = aws_subnet.private[*].id
  tags       = { Name = "${var.project_name}-db-subnets" }
}
resource "aws_db_instance" "ledger" {
  identifier              = var.project_name
  engine                  = "postgres"
  instance_class          = "db.t3.micro"
  allocated_storage       = 20
  max_allocated_storage   = 50
  storage_type            = "gp3"
  storage_encrypted       = true
  db_name                 = "ledger"
  username                = var.db_username
  password                = var.db_password
  db_subnet_group_name    = aws_db_subnet_group.ledger.name
  vpc_security_group_ids  = [aws_security_group.database.id]
  publicly_accessible     = false
  backup_retention_period = 1
  deletion_protection     = false
  skip_final_snapshot     = true
  tags = { Name = "${var.project_name}-postgres" }
}
