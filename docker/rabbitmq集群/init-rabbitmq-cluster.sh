#!/bin/bash

# 一些常量配置
MAIN_NODE=${MAIN_NODE:-rabbit@rabbitmq1}
WAIT_TIME=${WAIT_TIME:-10}
PLUGIN_URL="https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.13.0/rabbitmq_delayed_message_exchange-3.13.0.ez"
PLUGIN_PATH="/plugins/rabbitmq_delayed_message_exchange-3.13.0.ez"

# 安装 wget
if ! command -v wget &> /dev/null; then
    echo "wget not found, installing..."
    apt-get update && apt-get install -y wget
    if [ $? -ne 0 ]; then
        echo "Failed to install wget"
        exit 1
    fi
else
    echo "wget is already installed"
fi

# 创建插件目录，如果不存在的话
mkdir -p /plugins

# 下载 RabbitMQ 延迟消息插件
if [ ! -f "$PLUGIN_PATH" ]; then
    echo "Downloading rabbitmq_delayed_message_exchange plugin..."
    wget -O "$PLUGIN_PATH" "$PLUGIN_URL"
    if [ $? -ne 0 ]; then
        echo "Failed to download rabbitmq_delayed_message_exchange plugin"
        exit 1
    fi
else
    echo "Plugin already downloaded"
fi

# 修改插件文件权限
echo "Setting permissions for the plugin..."
chmod 644 "$PLUGIN_PATH"

# 启用插件
echo "Enabling the plugin..."
rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 启动 RabbitMQ 应用（以后台模式启动）
echo "Starting RabbitMQ server..."
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
