#!/usr/bin/env bash

# RabbitMQ 集群启动脚本。
# 主节点直接以前台模式启动；其他节点完成入群后再以前台模式运行。

set -euo pipefail

primary="${CLUSTER_PRIMARY:-rabbitmq1}"
current="$(hostname)"

if [[ "${current}" == "${primary}" ]]; then
  exec rabbitmq-server
fi

echo "等待 RabbitMQ 主节点 ${primary} 可用……"
until rabbitmq-diagnostics -q -n "rabbit@${primary}" ping; do
  sleep 3
done

echo "初始化当前节点并加入 rabbit@${primary}……"
rabbitmq-server -detached
rabbitmqctl await_startup
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl join_cluster "rabbit@${primary}"
rabbitmqctl stop

exec rabbitmq-server
