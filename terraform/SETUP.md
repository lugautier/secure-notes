# Terraform Setup & Deployment

## Prerequisites

- AWS account with credentials (Access Key ID + Secret Access Key)
- Terraform CLI installed (>= 1.6.0)
- Terraform Cloud account with organization `secure-notes`

## Quick Start (VCS-Driven Workflow)

### 1. Configure AWS Credentials in Terraform Cloud

Go to: https://app.terraform.io/app/secure-notes/workspaces/secure-notes-dev

**Settings** → **Variables** → **Add variable** (Environment Variables):

```
Name: AWS_ACCESS_KEY_ID
Value: <your-aws-access-key>
Category: Env var
Sensitive: ✅

Name: AWS_SECRET_ACCESS_KEY
Value: <your-aws-secret-key>
Category: Env var
Sensitive: ✅
```

### 2. Deploy Infrastructure

Push to main branch (auto-triggers VCS-driven workflow):

```bash
git push origin main
```

**In Terraform Cloud UI:**
1. Wait for plan to complete (displays 31 resources to create)
2. Click on the **Run**
3. Review the plan
4. Click **Confirm & Apply**
5. Infrastructure deploys automatically

**View progress:**
- Runs: https://app.terraform.io/app/secure-notes/workspaces/secure-notes-dev/runs
- State: https://app.terraform.io/app/secure-notes/workspaces/secure-notes-dev/states

## Local Development (Optional)

For local testing without Terraform Cloud approval:

```bash
# Authenticate with Terraform Cloud
terraform login app.terraform.io

# Navigate to terraform root
cd terraform

# Plan shows 31 resources (auto-loads dev.auto.tfvars)
terraform plan

# Apply locally (requires VCS workflow disabled - not recommended)
terraform apply
```

## Environment-Specific Configuration

**Non-sensitive variables** (version controlled):
- Located in: `terraform/dev.auto.tfvars` (auto-loaded)
- Examples: db_instance_class, aws_region, container_port, etc.

**Sensitive variables** (Terraform Cloud only):
- Set in workspace → Variables tab
- Examples: db_password, AWS credentials, API keys
- Marked as **Sensitive** ✅ in TFC

## Workspace Configuration

**Current workspace:** `secure-notes-dev`
- Working directory: `terraform/`
- VCS branch: main
- Auto-trigger: enabled

**For production** (future):
- Create new workspace: `secure-notes-prod`
- Set working directory: `terraform/`
- Add prod-specific variables in TFC
- Create `terraform/prod.auto.tfvars` in git
