resource "azurerm_postgresql_flexible_server" "pgsql_server" {
  administrator_login    = var.pgsql_admin_username
}

