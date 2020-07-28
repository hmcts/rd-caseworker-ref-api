#!/usr/bin/env sh

set -e

if [ -z "$POSTGRES_PASSWORD" ]; then
  echo "ERROR: Missing environment variables. Set value for '$POSTGRES_PASSWORD'."
  exit 1
fi

echo "Creating dbrdcaseworker database . . ."

psql -v ON_ERROR_STOP=1 --username postgres --dbname postgres <<-EOSQL
  CREATE ROLE dbrdcaseworker WITH PASSWORD 'dbrdcaseworker';
  CREATE DATABASE dbrdcaseworker ENCODING = 'UTF-8' CONNECTION LIMIT = -1;
  GRANT ALL PRIVILEGES ON DATABASE dbrdcaseworker TO dbrdcaseworker;
  ALTER ROLE dbrdcaseworker WITH LOGIN;
EOSQL

echo "Done creating database dbrdcaseworker."
