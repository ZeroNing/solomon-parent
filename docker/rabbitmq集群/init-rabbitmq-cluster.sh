#!/bin/bash

# 环境变量配置
MAIN_NODE=${MAIN_NODE:-rabbit@rabbitmq1}
WAIT_TIME=${WAIT_TIME:-10}

# 启动 RabbitMQ 应用
rabbitmq-server -detached

# 检查 RabbitMQ 是否完全启动
RETRIES=5
until rabbitmqctl status; do
  if [ $RETRIES -le 0 ]; then
    echo "RabbitMQ 未在预期时间内启动"
    exit 1
  fi
  echo "等待 RabbitMQ 启动..."
  RETRIES=$((RETRIES-1))
  sleep $WAIT_TIME
done

# 加入集群
if [ "$HOSTNAME" != "rabbitmq1" ]; then
  echo "节点 $HOSTNAME 加入集群 $MAIN_NODE"
  rabbitmqctl stop_app
  rabbitmqctl reset
  rabbitmqctl join_cluster $MAIN_NODE
  rabbitmqctl start_app
fi

echo "RabbitMQ 配置完成"

# 保持容器运行
tail -f /dev/null
