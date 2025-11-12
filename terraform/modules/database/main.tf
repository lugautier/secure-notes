# ========================================
# Database Module - RDS PostgreSQL
# ========================================

resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet-group-${var.environment}"
  subnet_ids = var.private_subnet_ids

  tags = merge(var.tags, { Name = "${var.project_name}-db-subnet-group" })
}

resource "aws_db_instance" "main" {
  identifier     = "${var.project_name}-db-${var.environment}"
  engine         = "postgres"
  engine_version = var.db_engine_version
  instance_class = var.db_instance_class

  db_name  = "securenotes"
  username = "postgres"
  password = "ChangeMe123!@#"

  allocated_storage       = var.db_allocated_storage
  storage_type            = "gp3"
  storage_encrypted       = true
  backup_retention_period = var.db_backup_retention_days
  multi_az                = var.db_multi_az
  publicly_accessible     = false

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [var.database_security_group_id]

  skip_final_snapshot       = var.environment == "dev" ? true : false
  final_snapshot_identifier = "${var.project_name}-db-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"

  tags = merge(var.tags, { Name = "${var.project_name}-db" })
}
