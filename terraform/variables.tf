# ========================================
# Global Input Variables
# ========================================
#
# Variable Sources:
#   📄 Git (terraform/environments/dev/terraform.tfvars):
#      - Non-sensitive infrastructure parameters
#      - Examples: db_instance_class, aws_region, environment, etc.
#      - Version controlled for auditability
#
#   🔒 Terraform Cloud (workspace variables):
#      - Sensitive values (passwords, API keys, private keys)
#      - Examples: db_password, jwt_private_key, encryption_key
#      - NEVER stored in git
#
# ========================================
# AWS Configuration
# ========================================

variable "aws_region" {
  description = "AWS region where resources will be deployed"
  type        = string
  default     = "eu-west-1"

  validation {
    condition     = can(regex("^[a-z]{2}-[a-z]+-\\d{1}$", var.aws_region))
    error_message = "AWS region must be a valid format (e.g., us-east-1, eu-west-1)"
  }
}

# ========================================
# Project Metadata
# ========================================

variable "project_name" {
  description = "Name of the project (used for resource naming and tagging)"
  type        = string
  default     = "secure-notes"

  validation {
    condition     = length(var.project_name) <= 20 && can(regex("^[a-z0-9-]+$", var.project_name))
    error_message = "Project name must be lowercase alphanumeric with hyphens, max 20 chars"
  }
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be one of: dev, staging, prod"
  }
}

# ========================================
# Networking Configuration
# ========================================

variable "vpc_cidr_block" {
  description = "CIDR block for the VPC (10.0.0.0/16 = 65,536 IP addresses)"
  type        = string
  default     = "10.0.0.0/16"

  validation {
    condition     = can(cidrhost(var.vpc_cidr_block, 0))
    error_message = "VPC CIDR block must be a valid CIDR notation"
  }
}

variable "public_subnet_cidrs" {
  description = "List of CIDR blocks for public subnets (for ALB/NAT Gateway in future)"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "List of CIDR blocks for private subnets (for RDS and ECS)"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.11.0/24"]
}

variable "availability_zones" {
  description = "List of availability zones (AWS data centers within the region)"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b", "eu-west-1"]

  validation {
    condition     = length(var.availability_zones) >= 2
    error_message = "Must have at least 2 availability zones for high availability"
  }
}

# ========================================
# Database Configuration
# ========================================

variable "db_instance_class" {
  description = "RDS instance type (t3.micro = free tier eligible, t3.small = prod)"
  type        = string

  validation {
    condition     = can(regex("^t3\\.(micro|small|medium)$", var.db_instance_class))
    error_message = "Instance class must be t3.micro, t3.small, or t3.medium"
  }
}

variable "db_allocated_storage" {
  description = "Storage size in GB (20 = minimum, free tier eligible)"
  type        = number

  validation {
    condition     = var.db_allocated_storage >= 20 && var.db_allocated_storage <= 1000
    error_message = "Storage must be between 20 GB and 1000 GB"
  }
}

variable "db_engine_version" {
  description = "PostgreSQL version (e.g., 16.1, 15.5)"
  type        = string
  default     = "16.1"

  validation {
    condition     = can(regex("^(14|15|16)\\.[0-9]+$", var.db_engine_version))
    error_message = "PostgreSQL version must be 14.x, 15.x, or 16.x"
  }
}

variable "db_backup_retention_days" {
  description = "Number of days to retain backups (1 = free tier, 7+ = production)"
  type        = number
  default     = 7

  validation {
    condition     = var.db_backup_retention_days >= 1 && var.db_backup_retention_days <= 35
    error_message = "Backup retention must be between 1 and 35 days (1 for free tier, 7+ for production)"
  }
}

variable "db_multi_az" {
  description = "Enable Multi-AZ deployment (high availability, doubles cost)"
  type        = bool
  # false for dev (cost savings), true for prod (reliability)
}

# ========================================
# Container (ECS) Configuration
# ========================================

variable "container_image" {
  description = "Docker image URI (e.g., 123456789.dkr.ecr.eu-west-1.amazonaws.com/secure-notes:latest)"
  type        = string
}

variable "container_port" {
  description = "Port the application listens on inside the container"
  type        = number
  default     = 8080

  validation {
    condition     = var.container_port >= 1024 && var.container_port <= 65535
    error_message = "Container port must be between 1024 and 65535"
  }
}

variable "desired_task_count" {
  description = "Number of container instances to run (1 = dev, 3 = prod HA)"
  type        = number

  validation {
    condition     = var.desired_task_count >= 1 && var.desired_task_count <= 10
    error_message = "Task count must be between 1 and 10"
  }
}

variable "task_cpu" {
  description = "CPU units allocated to each task (256 = 0.25 vCPU)"
  type        = number

  validation {
    condition     = contains([256, 512, 1024, 2048], var.task_cpu)
    error_message = "CPU must be 256, 512, 1024, or 2048 units"
  }
}

variable "task_memory" {
  description = "Memory (MB) allocated to each task (512 = 512 MB)"
  type        = number

  validation {
    condition     = contains([512, 1024, 2048, 4096], var.task_memory)
    error_message = "Memory must be 512, 1024, 2048, or 4096 MB"
  }
}

# ========================================
# Cost Optimization & Tagging
# ========================================

variable "cost_center" {
  description = "Cost center for billing and FinOps tracking"
  type        = string
  default     = "engineering"
}

variable "owner_email" {
  description = "Email of resource owner (for notifications and FinOps alerts)"
  type        = string

  validation {
    condition     = can(regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", var.owner_email))
    error_message = "Owner email must be a valid email address"
  }
}

# ========================================
# 🔒 SENSITIVE VARIABLES (Terraform Cloud Only)
# ========================================
# These are stored in Terraform Cloud workspace variables with "Sensitive" checkbox
# They are NEVER stored in git or terraform.tfvars

variable "db_password" {
  description = "RDS PostgreSQL master user password (set in Terraform Cloud for production)"
  type        = string
  sensitive   = true
  default     = "DevPassword123456!"

  validation {
    condition     = length(var.db_password) >= 12
    error_message = "Database password must be at least 12 characters"
  }
}
