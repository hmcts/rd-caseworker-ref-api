output "username" {
  value = join("@", [var.database_name, join("-", [var.product-V15, var.component-V15])])
}
