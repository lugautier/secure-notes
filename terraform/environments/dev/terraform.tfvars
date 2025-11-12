# ========================================
# Development Environment Configuration
# ========================================

# Project
project_name = "secure-notes"
environment  = "dev"
owner_email  = "lgautier@norsys.fr"

# AWS
aws_region = "eu-west-1"

# Networking
vpc_cidr_block       = "10.0.0.0/16"
public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
private_subnet_cidrs = ["10.0.10.0/24", "10.0.11.0/24"]
availability_zones   = ["eu-west-1a", "eu-west-1b"]

# Database (dev: minimal, cost-optimized)
db_instance_class       = "t3.micro"
db_allocated_storage    = 20
db_engine_version       = "16.1"
db_backup_retention_days = 7
db_multi_az             = false

# Compute (dev: single container instance)
container_image    = "nginx:latest"
container_port     = 8080
desired_task_count = 1
task_cpu           = 256
task_memory        = 512

# Cost Management
cost_center = "engineering"
