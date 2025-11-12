# Terraform Infrastructure

This directory contains Infrastructure as Code (IaC) for the secure-notes application using **Terraform** with **Terraform Cloud**.

## Overview

### What is Terraform?

Terraform is an Infrastructure as Code tool that lets you define cloud infrastructure using code files (`.tf` files) instead of clicking buttons in the AWS console.

**Benefits:**
- **Reproducible**: Same code = same infrastructure every time
- **Versionable**: Track infrastructure changes in git like application code
- **Auditable**: See who changed what and when
- **Automatable**: Integrate with CI/CD pipelines

### Why Terraform Cloud?

We use **Terraform Cloud** instead of local state files for:

1. **State Management**: Terraform tracks infrastructure state in a safe, encrypted cloud service (no S3 bucket to manage)
2. **Locking**: Prevents concurrent operations that could corrupt state
3. **VCS Integration**: Automatic `terraform plan` on git push
4. **Approval Workflow**: Manual review before infrastructure changes
5. **Audit Trail**: Full history of who deployed what and when

---

## Architecture

### Module Structure

```
terraform/
├── main.tf              # Root configuration, module definitions
├── variables.tf         # Global input variables
├── backend.tf           # Terraform Cloud backend config (reference only)
├── SETUP.md             # Quick reference commands
├── README.md            # This file (explanations)
│
├── modules/
│   ├── network/         # VPC, subnets, security groups
│   │   ├── main.tf        
│   │   ├── variables.tf   # Module inputs
│   │   └── outputs.tf     # Module outputs
│   ├── security/        # IAM roles, Secrets Manager
│   ├── database/        # RDS PostgreSQL
│   └── compute/         # ECS Fargate cluster
│
└── environments/          # Environment-specific values
    └── dev/
        └── terraform.tfvars     # Dev environment values (non-sensitive only)
```

### Module Descriptions

**network**: VPC foundation
- 1 VPC (10.0.0.0/16)
- 2 public subnets (internet-accessible)
- 2 private subnets (hidden, for database & containers)
- 2 security groups (firewall rules)
- Route tables for traffic management

**security**: Access control & secrets
- ECS Task Execution Role (for ECS to manage containers)
- ECS Task Role (for app to access AWS services)
- 3 Secrets Manager secrets (database credentials, JWT, encryption key)

**database**: Data persistence
- RDS PostgreSQL instance
- Encrypted storage
- Configurable backups and high availability

**compute**: Application runtime
- ECS Fargate cluster (serverless container orchestration)
- CloudWatch log group (container logs)
- ECS Task Definition (blueprint for running containers)
- ECS Service (keeps containers running 24/7)

---

## VCS-Driven Workflow

### What is VCS-Driven?

VCS = Version Control System (Git)

**Traditional Workflow:**
```
You run: terraform plan
         terraform apply
         (from your machine)
```

**VCS-Driven Workflow:**
```
You push code to main branch
         ↓
GitHub webhook → Terraform Cloud
         ↓
Terraform Cloud runs: terraform plan (automatic)
         ↓
You review plan in Terraform Cloud UI
         ↓
You click: Approve & Apply
         ↓
Terraform Cloud runs: terraform apply (automatic)
         ↓
Infrastructure deployed
```

### Why VCS-Driven?

1. **Code Review**: Every infrastructure change goes through git (audit trail)
2. **Safety**: Requires approval before deploying
3. **Automation**: No manual commands needed
4. **Consistency**: Same process every time
5. **Reversibility**: Can rollback by reverting git commit

### How It Works

1. **Trigger**: Push to `main` branch
   ```bash
   git push origin main
   ```

2. **Detection**: GitHub sends webhook to Terraform Cloud

3. **Planning**: Terraform Cloud runs `terraform plan`
   - Analyzes `.tf` files
   - Compares against current AWS state
   - Shows what will be created/modified/deleted

4. **Review**: You review the plan in Terraform Cloud UI

5. **Approval**: Click "Confirm & Apply"

6. **Apply**: Terraform Cloud runs `terraform apply`
   - Creates/modifies/deletes AWS resources
   - Updates state in Terraform Cloud

---

## Important: RDS Database Disabled

**Status**: The RDS PostgreSQL database module is currently **disabled** in this deployment.

**Why**: AWS free tier is not available for RDS on this account. The module was disabled to allow the rest of the infrastructure (VPC, ECS, Security Groups, IAM) to deploy successfully.

**How it's disabled**:
- The `module "database"` block in `main.tf` is commented out
- Database references in the compute module use placeholder values:
  ```hcl
  db_endpoint = "localhost"  # Placeholder
  db_port     = 5432
  db_name     = "securenotes"
  ```

**To re-enable RDS later**:
1. Uncomment the `module "database"` block in `main.tf`
2. Update the compute module to use `module.database.db_endpoint` instead of placeholder
3. Ensure your AWS account has RDS free tier eligibility OR upgrade to a paid plan
4. Run `terraform plan` and `terraform apply`

**Alternative solutions**:
- Use a different AWS account with free tier RDS eligibility
- Upgrade to AWS paid plan
- Use alternative database (DynamoDB, managed PostgreSQL outside free tier, etc.)

---

## Environments

### Variable Strategy (Hybrid Approach)

**📄 Non-Sensitive Variables** (version controlled in git):
- Infrastructure parameters: `db_instance_class`, `aws_region`, `environment`, `task_cpu`, `desired_task_count`, etc.
- Stored in: `terraform/environments/{env}/terraform.tfvars`
- Why: Auditable in git history, reviewable in PRs, easy to compare dev vs prod configurations

**🔒 Sensitive Variables** (NEVER in git):
- Database passwords, JWT private keys, encryption keys, API tokens
- Stored in: **Terraform Cloud workspace UI** → **Variables** tab with **Sensitive** checkbox ✅
- Why: Protected from accidental exposure, can rotate without code change, encrypted in transit/storage

### Dev Environment

**Location**:
- Configuration: `terraform/environments/dev/terraform.tfvars`
- Workspace: `secure-notes-dev` (in Terraform Cloud)

**Non-Sensitive Values** (in terraform.tfvars):
```hcl
db_instance_class    = "t3.micro"      # Cost-optimized for development
desired_task_count   = 1               # Single container instance
db_multi_az          = false           # No redundancy needed
db_backup_retention_days = 7           # Minimal backup retention
```

**Sensitive Values** (set in Terraform Cloud workspace `secure-notes-dev`):
- `db_password` - RDS master password (minimum 12 characters)

### Future: Prod Environment

When adding production :
- Create a new workspace in Terraform Cloud (`secure-notes-prod`) with required variables
- Create tfvars file**: `terraform/environments/prod/terraform.tfvars`
- Remove hardcoded `secure-notes-prod` in main.tf and handle workspace selection

Each workspace has:
- ✅ Independent state (isolated infrastructure)
- ✅ Independent sensitive variables (different passwords per env)
- ✅ Independent approval workflow (separate plan/apply reviews)
- ✅ Automatic VCS-driven triggers

---

## File Organization

```
terraform/
├── README.md              # This file (explanations)
├── SETUP.md               # Commands to launch
├── backend.tf             # References TFC backend (no changes needed)
├── main.tf                # Module definitions
├── variables.tf           # Global variables
│
├── modules/               # Reusable components
│   ├── network/
│   │   ├── main.tf        # VPC, subnets, security groups
│   │   ├── variables.tf   # Module inputs
│   │   └── outputs.tf     # Module outputs
│   ├── security/
│   ├── database/
│   └── compute/
│
└── environments/          # Environment-specific values
    └── dev/
        └── terraform.tfvars # Dev-specific non-sensitive variables
```

---

## References

- [Terraform Documentation](https://www.terraform.io/docs)
- [Terraform Cloud Documentation](https://developer.hashicorp.com/terraform/cloud-docs)
- [AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
