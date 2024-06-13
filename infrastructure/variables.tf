variable "product" {
  type = string
}

variable "raw_product" {
  type    = string
  default = "rd" // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {
  type = string
}

variable "location" {
  type    = string
  default = "UK South"
}

variable "env" {
  type = string
}

variable "subscription" {
  type = string
}

variable "common_tags" {
  type = map(string)
}

variable "team_name" {
  type    = string
  default = "RD"
}

variable "postgresql_version" {
  type    = string
  default = "11"
}

variable "db_replicas" {
  type    = list(string)
  default = []
}


variable "product-v16" {
  type = string
  default="rd-caseworker-ref-api"
}

variable "component-v16" {
  type = string
  default="postgres-db-v16"
}
variable "aks_subscription_id" {
}
variable "team_contact" {
  type        = string
  description = "The name of your Slack channel people can use to contact your team about your infrastructure"
  default     = "#refdata-pet"
}
variable "destroy_me" {
  type        = string
  description = "In the future if this is set to Yes then automation will delete this resource on a schedule. Please set to No unless you know what you are doing"
  default     = "No"
}
variable "sku" {
  type        = string
  default     = "Premium"
  description = "SKU type(Basic, Standard and Premium)"
}

variable "tenant_id" {
  type        = string
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environment variables and not normally required to be specified."
}

variable "force_user_permissions_trigger" {
  default     = ""
  type        = string
  description = "Update this to a new value to force the user permissions script to run again"
}

variable "jenkins_AAD_objectId" {
  type        = string
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "enable_schema_ownership" {
  type        = bool
  default     = false
  description = "Enables the schema ownership script. Change this to true if you want to use the script. Defaults to false"
}

variable "force_schema_ownership_trigger" {
  default     = ""
  type        = string
  description = "Update this to a new value to force the schema ownership script to run again."
}

variable "kv_subscription" {
  default     = "DCD-CNP-DEV"
  type        = string
  description = "Update this with the name of the subscription where the single server key vault is. Defaults to DCD-CNP-DEV."
}

variable "enable_replica" {
  description = "Flag to enable the creation of a PostgreSQL Flexible server replica"
  type        = bool
  default     = false
}
variable "primary_server_id" {
  description = "Azure resource ID of the primary PostgreSQL server"
  type        = string
  default     = "not_applicable" // Dummy Value for none replica environments
}

variable "pgsql_server_configuration" {
  description = "Postgres server configuration"
  type        = list(object({ name : string, value : string }))
  default = [
    {
      name  = "azure.extensions"
      value = "PLPGSQL,PG_STAT_STATEMENTS,PG_BUFFERCACHE"
    },
    {
      name  = "backslash_quote"
      value = "ON"
    },
    {
      name  = "azure.enable_temp_tablespaces_on_local_ssd"
      value = "OFF"
    }
  ]
}

variable "pgsql_sku" {
  description = "The PGSql flexible server instance sku"
  default     =  "GP_Standard_D4s_v3"
}