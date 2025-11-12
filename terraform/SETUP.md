# Terraform Setup Guide

This document provides step-by-step instructions for setting up Terraform for the secure-notes project.

## Prerequisites

- AWS CLI installed and configured with credentials
- Terraform >= 1.6.0 installed
- AWS account with appropriate permissions (IAM, S3, DynamoDB, RDS, ECS, VPC)

## Step 1: Get Your AWS Account ID

```bash
aws sts get-caller-identity --query Account --output text
```

Save this value - you'll need it in the next steps.

---

## Step 2: Create S3 Bucket for Terraform State

Replace `<YOUR_ACCOUNT_ID>` with your actual AWS account ID:

```bash
aws s3api create-bucket \
  --bucket secure-notes-terraform-state-<YOUR_ACCOUNT_ID> \
  --region eu-west-1 \
  --create-bucket-configuration LocationConstraint=eu-west-1
```

**Note**: S3 bucket names must be globally unique across all AWS accounts. The account ID ensures uniqueness.

---

## Step 3: Enable Versioning on S3 Bucket

```bash
aws s3api put-bucket-versioning \
  --bucket secure-notes-terraform-state-<YOUR_ACCOUNT_ID> \
  --versioning-configuration Status=Enabled
```

**Why**: Versioning allows you to recover previous Terraform states if something goes wrong.

---

## Step 4: Block Public Access to S3 Bucket

```bash
aws s3api put-public-access-block \
  --bucket secure-notes-terraform-state-<YOUR_ACCOUNT_ID> \
  --public-access-block-configuration \
  "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"
```

**Why**: Terraform state contains sensitive information (passwords, keys). Never allow public access.

---

## Step 5: Create DynamoDB Table for State Locking

```bash
aws dynamodb create-table \
  --table-name secure-notes-terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region eu-west-1
```

**Why**: State locking prevents concurrent Terraform operations from corrupting the state file.

---

## Step 6: Update Terraform Configuration with Your S3 Bucket Name

Edit `terraform/environments/dev/main.tf` and update the bucket name:

```hcl
terraform {
  backend "s3" {
    bucket         = "secure-notes-terraform-state-<YOUR_ACCOUNT_ID>"  # Update this line
    key            = "dev/terraform.tfstate"
    region         = "eu-west-1"
    dynamodb_table = "secure-notes-terraform-locks"
    encrypt        = true
  }
}
```

Replace `<YOUR_ACCOUNT_ID>` with your actual AWS account ID.

---

## Step 7: Initialize Terraform

Navigate to the dev environment directory and initialize Terraform:

```bash
cd terraform/environments/dev
terraform init
```

**What happens**:
- Terraform downloads required providers (AWS)
- Creates `.terraform` directory
- Initializes the remote S3 backend
- Validates configuration files

**Expected output**: "Terraform has been successfully initialized!"

---

## Step 8: Review Infrastructure Plan

Before applying, always review what Terraform will create:

```bash
terraform plan -out=tfplan
```

**What this does**:
- Analyzes your configuration
- Compares against the current AWS state
- Shows all resources that will be created/modified/deleted
- Saves the plan to `tfplan` file

**Review the output carefully** - look for:
- Expected resource types (VPC, RDS, ECS, etc.)
- Correct values (region, instance types, etc.)
- No unexpected deletions

---

## Step 9: Apply Configuration (Deploy to AWS)

Once you're satisfied with the plan:

```bash
terraform apply tfplan
```

**What happens**:
- Terraform creates all resources in AWS
- Uploads state to S3
- Can take 10-30 minutes (especially RDS)

**Monitor the output** for any errors.

---

## Step 10: Verify Deployment

Check that resources were created:

```bash
# Show all outputs
terraform output

# Or check specific resources in AWS Console:
# - VPC: eu-west-1 region
# - RDS: secure-notes-db-dev instance
# - ECS: secure-notes-cluster-dev cluster
# - Secrets Manager: 3 secrets created
```

---

## Cleanup (Optional - for Development)

If you need to tear down the infrastructure (e.g., to save costs):

```bash
terraform destroy
```

**Warning**: This will delete all resources created by Terraform. Use only for dev environment.

---

## Troubleshooting

### Error: "no AWS credentials provided"

```bash
aws configure
# or export AWS credentials:
export AWS_ACCESS_KEY_ID=<your-key>
export AWS_SECRET_ACCESS_KEY=<your-secret>
export AWS_DEFAULT_REGION=eu-west-1
```

### Error: "S3 bucket already exists"

S3 bucket names are globally unique. Try a different account ID or suffix:

```bash
aws s3api create-bucket \
  --bucket secure-notes-terraform-state-<YOUR_ACCOUNT_ID>-v2 \
  --region eu-west-1 \
  --create-bucket-configuration LocationConstraint=eu-west-1
```

### Error: "AccessDenied" when creating resources

Check that your AWS user has permissions for:
- EC2 (VPC, subnets, security groups)
- RDS
- ECS
- IAM (roles, policies)
- Secrets Manager
- CloudWatch

Consider asking your AWS administrator for `PowerUserAccess` policy or equivalent.

### Error: "DynamoDB table already exists"

The table was already created in a previous run. You can safely proceed - Terraform will use the existing table.

---

## State File Management

### Local State (Development Only)

For local development testing without S3:

```hcl
# Comment out the backend block in main.tf
# terraform {
#   backend "s3" { ... }
# }
```

State will be stored locally in `.terraform/tfstate`. **Never commit this to Git!**

### Multiple Environments

For production, create `terraform/environments/prod/` with its own:
- `terraform.tfvars` (prod values: multi-AZ, larger instances)
- `main.tf` (backend key: `prod/terraform.tfstate`)

```bash
cd terraform/environments/prod
terraform init
terraform plan
terraform apply
```

---

## References

- [Terraform S3 Backend Documentation](https://www.terraform.io/language/settings/backends/s3)
- [AWS S3 Create Bucket CLI](https://docs.aws.amazon.com/cli/latest/reference/s3api/create-bucket.html)
- [Terraform Best Practices](https://www.terraform.io/docs/cloud/guides/recommended-practices)
