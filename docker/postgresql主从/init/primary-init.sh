#!/usr/bin/env bash

# 创建流复制账号，并允许副本节点建立 replication 连接。
set -e

psql \
  --set=ON_ERROR_STOP=1 \
  --username "${POSTGRES_USER}" \
  --dbname "${POSTGRES_DB}" \
  --set=replication_password="${POSTGRES_REPLICATION_PASSWORD}" <<'SQL'
CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD :'replication_password';
SQL

echo "host replication replicator all scram-sha-256" >> "${PGDATA}/pg_hba.conf"
