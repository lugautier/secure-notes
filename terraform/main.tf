# ========================================
# Terraform Configuration with Cloud Backend
# ========================================
terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  cloud {
    organization = "secure-notes"

    workspaces {
      # only dev workspace for now
      name = "secure-notes-dev"
    }
  }
}

# ========================================
# AWS Provider Configuration
# ========================================
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      CreatedAt   = timestamp()
    }
  }
}

# ========================================
# Network Module
# ========================================
module "network" {
  source = "./modules/network"

  project_name = var.project_name
  environment  = var.environment

  vpc_cidr_block         = var.vpc_cidr_block
  public_subnet_cidrs    = var.public_subnet_cidrs
  private_subnet_cidrs   = var.private_subnet_cidrs
  availability_zones     = var.availability_zones

  tags = local.common_tags
}

# ========================================
# Security Module
# ========================================
module "security" {
  source = "./modules/security"

  project_name = var.project_name
  environment  = var.environment

  tags = local.common_tags
}

# ========================================
# Database Module
# ========================================
module "database" {
  source = "./modules/database"

  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region

  private_subnet_ids             = module.network.private_subnet_ids
  database_security_group_id     = module.network.database_security_group_id

  db_instance_class       = var.db_instance_class
  db_allocated_storage    = var.db_allocated_storage
  db_engine_version       = var.db_engine_version
  db_backup_retention_days = var.db_backup_retention_days
  db_multi_az              = var.db_multi_az
  db_password              = var.db_password

  db_credentials_secret_arn = module.security.db_credentials_secret_arn

  tags = local.common_tags
}

# ========================================
# Compute Module
# ========================================
module "compute" {
  source = "./modules/compute"

  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region

  private_subnet_ids                = module.network.private_subnet_ids
  app_security_group_id             = module.network.app_security_group_id

  ecs_task_execution_role_arn = module.security.ecs_task_execution_role_arn
  ecs_task_role_arn           = module.security.ecs_task_role_arn

  db_endpoint = module.database.db_endpoint
  db_port     = module.database.db_port
  db_name     = module.database.db_name

  container_image      = var.container_image
  container_port       = var.container_port
  desired_task_count   = var.desired_task_count
  task_cpu             = var.task_cpu
  task_memory          = var.task_memory

  db_credentials_secret_arn = module.security.db_credentials_secret_arn
  jwt_secret_arn            = module.security.jwt_secret_arn
  encryption_key_arn        = module.security.encryption_key_arn

  tags = local.common_tags
}

# ========================================
# Local Variables
# ========================================
locals {
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}
