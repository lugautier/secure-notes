# ========================================
# Terraform Remote State Configuration
# ========================================
terraform {
  backend "s3" {
    bucket = "secure-notes-terraform-state-093229128057"
    key = "dev/terraform.tfstate"
    region = "eu-west-1"
    dynamodb_table = "secure-notes-terraform-locks"
    encrypt = true
  }
}
