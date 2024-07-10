locals {
  preview_vault_name     = join("-", [var.raw_product, "aat"])
  non_preview_vault_name = join("-", [var.raw_product, var.env])
  key_vault_name         = var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name

  s2s_rg_prefix            = "rpe-service-auth-provider"
  s2s_key_vault_name       = var.env == "preview" || var.env == "spreview" ? join("-", ["s2s", "aat"]) : join("-", ["s2s", var.env])
  s2s_vault_resource_group = var.env == "preview" || var.env == "spreview" ? join("-", [local.s2s_rg_prefix, "aat"]) : join("-", [local.s2s_rg_prefix, var.env])
}

data "azurerm_key_vault" "rd_key_vault" {
  name                = local.key_vault_name
  resource_group_name = local.key_vault_name
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = local.s2s_key_vault_name
  resource_group_name = local.s2s_vault_resource_group
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name         = "microservicekey-rd-caseworker-ref-api"
  key_vault_id = data.azurerm_key_vault.s2s_key_vault.id
}

resource "azurerm_key_vault_secret" "caseworker_s2s_secret" {
  name         = "caseworker-ref-api-s2s-secret"
  value        = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id = data.azurerm_key_vault.rd_key_vault.id
}

# Create the database server v16
# Name and resource group name will be defaults (<product>-<component>-<env> and <product>-<component>-data-<env> respectively)
module "db-rd-caseworker-ref-v16" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"

  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "cft"
  common_tags          = var.common_tags
  component            = var.component-v16
  env                  = var.env
  pgsql_databases = [
    {
      name = "dbrdcaseworker"
    }
  ]

  # Setup Access Reader db user
  force_user_permissions_trigger = "3"

  # Sets correct DB owner after migration to fix permissions
  enable_schema_ownership        = var.enable_schema_ownership
  force_schema_ownership_trigger = "3"
  kv_subscription                = var.kv_subscription
  kv_name                        = data.azurerm_key_vault.rd_key_vault.name
  user_secret_name               = azurerm_key_vault_secret.POSTGRES-USER.name
  pass_secret_name               = azurerm_key_vault_secret.POSTGRES-PASS.name

  subnet_suffix = "expanded"
  pgsql_version = "16"
  product       = "rd"
  name          = join("-", [var.product-v16, var.component-v16])

  pgsql_server_configuration = var.pgsql_server_configuration

}

# Create replica server instance
module "db-rd-caseworker-ref-v16-replica" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  count  = var.enable_replica ? 1 : 0
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "cft"
  common_tags          = var.common_tags
  component            = var.component-v16
  env                  = var.env
  pgsql_databases = [
    {
      name = "dbrdcaseworker"
    }
  ]

  # Setup Access Reader db user
  force_user_permissions_trigger = "3"

  # Sets correct DB owner after migration to fix permissions
  enable_schema_ownership        = var.enable_schema_ownership
  force_schema_ownership_trigger = "3"
  kv_subscription                = var.kv_subscription
  kv_name                        = data.azurerm_key_vault.rd_key_vault.name
  user_secret_name               = azurerm_key_vault_secret.POSTGRES-USER.name
  pass_secret_name               = azurerm_key_vault_secret.POSTGRES-PASS.name

  subnet_suffix              = "expanded"
  pgsql_version              = "16"
  product                    = "rd"
  name                       = join("-", [var.product-v16, var.component-v16, "replica"])
  resource_group_name        = "rd-caseworker-ref-api-postgres-db-v16-data-${var.env}"
  create_mode                = "Replica"
  source_server_id           = var.primary_server_id
  pgsql_server_configuration = var.pgsql_server_configuration

}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = join("-", [var.component, "POSTGRES-PASS"])
  value        = module.db-rd-caseworker-ref-v16.password
  key_vault_id = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name         = join("-", [var.component, "POSTGRES-DATABASE"])
  value        = "dbrdcaseworker"
  key_vault_id = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name         = join("-", [var.component, "POSTGRES-HOST"])
  value        = module.db-rd-caseworker-ref-v16.fqdn
  key_vault_id = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name         = join("-", [var.component, "POSTGRES-PORT"])
  value        = "5432"
  key_vault_id = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = join("-", [var.component, "POSTGRES-USER"])
  value        = module.db-rd-caseworker-ref-v16.username
  key_vault_id = data.azurerm_key_vault.rd_key_vault.id
}