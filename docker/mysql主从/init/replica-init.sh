#!/usr/bin/env bash

# 等待主库复制账号可用，然后为副本配置 GTID 自动复制。
set -e

until MYSQL_PWD="${MYSQL_REPLICATION_PASSWORD}" mysql \
  --host=mysql-master \
  --user=replicator \
  --execute="SELECT 1" >/dev/null 2>&1; do
  echo "等待 MySQL 主库可用……"
  sleep 3
done

docker_process_sql <<SQL
CHANGE REPLICATION SOURCE TO
  SOURCE_HOST='mysql-master',
  SOURCE_PORT=3306,
  SOURCE_USER='replicator',
  SOURCE_PASSWORD='${MYSQL_REPLICATION_PASSWORD}',
  SOURCE_AUTO_POSITION=1,
  GET_SOURCE_PUBLIC_KEY=1;
START REPLICA;
SQL
