#!/usr/bin/env bash

# 首次启动时从主节点执行基础备份，之后直接使用已持久化的数据启动热备。
set -euo pipefail

if [[ ! -s "${PGDATA}/PG_VERSION" ]]; then
  mkdir -p "${PGDATA}"
  chown -R postgres:postgres "${PGDATA}"

  until pg_isready -h postgres-primary -p 5432; do
    echo "等待 PostgreSQL 主节点可用……"
    sleep 3
  done

  rm -rf "${PGDATA:?}/"*
  export PGPASSWORD="${POSTGRES_REPLICATION_PASSWORD}"
  gosu postgres pg_basebackup \
    --host=postgres-primary \
    --port=5432 \
    --username=replicator \
    --pgdata="${PGDATA}" \
    --wal-method=stream \
    --write-recovery-conf \
    --progress
fi

exec docker-entrypoint.sh postgres
