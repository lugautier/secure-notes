# ========================================
# Compute Module Variables
# ========================================

variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "aws_region" {
  description = "AWS region"
  type        = string
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs for ECS"
  type        = list(string)
}

variable "app_security_group_id" {
  description = "Security group ID for ECS tasks"
  type        = string
}

variable "ecs_task_execution_role_arn" {
  description = "ARN of ECS task execution role"
  type        = string
}

variable "ecs_task_role_arn" {
  description = "ARN of ECS task role"
  type        = string
}

variable "db_endpoint" {
  description = "Database endpoint"
  type        = string
}

variable "db_port" {
  description = "Database port"
  type        = number
}

variable "db_name" {
  description = "Database name"
  type        = string
}

variable "container_image" {
  description = "Docker image URI"
  type        = string
}

variable "container_port" {
  description = "Container port"
  type        = number
}

variable "desired_task_count" {
  description = "Number of ECS tasks to run"
  type        = number
}

variable "task_cpu" {
  description = "CPU units for each task"
  type        = number
}

variable "task_memory" {
  description = "Memory (MB) for each task"
  type        = number
}

variable "db_credentials_secret_arn" {
  description = "ARN of database credentials secret"
  type        = string
}

variable "jwt_secret_arn" {
  description = "ARN of JWT secret"
  type        = string
}

variable "encryption_key_arn" {
  description = "ARN of encryption key secret"
  type        = string
}

variable "tags" {
  description = "Common tags for all resources"
  type        = map(string)
  default     = {}
}
