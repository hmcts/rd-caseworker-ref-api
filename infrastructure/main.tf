locals {
  local_env               = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env
  preview_vault_name      = join("-", [var.raw_product, "aat"])
  non_preview_vault_name  = join("-", [var.raw_product, var.env])
  key_vault_name          = var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name
}

data "azurerm_key_vault" "rd_key_vault" {
  name                = local.key_vault_name
  resource_group_name = local.key_vault_name
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name          = join("-", [var.component, "POSTGRES-PASS"])
  value         = module.db-rd-caseworker-ref-api.postgresql_password
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

module "db-rd-caseworker-ref-api" {
  source              = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product             = join("-", [var.product, var.component, "postgres-db"])
  location            = var.location
  subscription        = var.subscription
  env                 = var.env
  postgresql_user     = "dbrdcaseworker"
  database_name       = "dbrdcaseworker"
  common_tags         = var.common_tags
  postgresql_version  = var.postgresql_version
}

data "azurerm_key_vault" "key_vault" {
  name = "${var.raw_product}-${var.env}"
  resource_group_name = "${var.raw_product}-${var.env}"
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}


data "azurerm_key_vault_secret" "s2s_secret" {
  key_vault_id = data.azurerm_key_vault.s2s_key_vault.id
  name      = "microservicekey-rd-caseworker-ref-api"
}

resource "azurerm_key_vault_secret" "rd_caseworker_ref_api_app_s2s_secret" {
  name         = "s2s-secret-rd-caseworker-ref-api"
  value        = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id = data.azurerm_key_vault.key_vault.id
}
