provider "azurerm" {
  version = "=1.44.0"
}

locals {
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  preview_app_service_plan = "${var.product}-${var.component}-${var.env}"
  non_preview_app_service_plan = "${var.product}-${var.env}"
  app_service_plan = "${var.env == "preview" || var.env == "spreview" ? local.preview_app_service_plan : local.non_preview_app_service_plan}"

  preview_vault_name = "${var.raw_product}-aat"
  non_preview_vault_name = "${var.raw_product}-${var.env}"
  key_vault_name = "${var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name}"

}

data "azurerm_key_vault" "rd_key_vault" {
  name = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
}

data "azurerm_key_vault" "s2s_key_vault" {
  name = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "s2s_microservice" {
  name = "s2s-microservice"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "s2s_url" {
  name = "s2s-url"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "microservicekey-rd-caseworker-ref-api"
  key_vault_id = "${data.azurerm_key_vault.s2s_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name      = "${var.component}-POSTGRES-USER"
  value     = "${module.db-rd-caseworker-ref-api.user_name}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name      = "${var.component}-POSTGRES-PASS"
  value     = "${module.db-rd-caseworker-ref-api.postgresql_password}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name      = "${var.component}-POSTGRES-HOST"
  value     = "${module.db-rd-caseworker-ref-api.host_name}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name      = "${var.component}-POSTGRES-PORT"
  value     = "5432"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name      = "${var.component}-POSTGRES-DATABASE"
  value     = "${module.db-rd-caseworker-ref-api.postgresql_database}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

module "db-rd-caseworker-ref-api" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${var.product}-${var.component}-postgres-db"
  location = "${var.location}"
  subscription = "${var.subscription}"
  env = "${var.env}"
  postgresql_user = "dbrdcaseworker"
  database_name = "dbrdcaseworker"
  common_tags = "${var.common_tags}"
  postgresql_version    = "${var.postgresql_version}"

}
