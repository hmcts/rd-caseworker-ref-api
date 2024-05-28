db_replicas = [ "replica" ]
pgsql_server_configuration = [
  {
    name  = "azure.extensions"
    value = "PLPGSQL"
  },
  {
    name  = "backslash_quote"
    value = "ON"
  }]
# PG Flexible Server replica enable for this env (AAT)
enable_replica = true