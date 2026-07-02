#!/usr/bin/env bash

# 创建仅用于复制的账号；该脚本只在主库数据目录首次初始化时执行。
set -e

docker_process_sql <<SQL
CREATE USER IF NOT EXISTS 'replicator'@'%' IDENTIFIED BY '${MYSQL_REPLICATION_PASSWORD}';
GRANT REPLICATION SLAVE ON *.* TO 'replicator'@'%';
FLUSH PRIVILEGES;
SQL
