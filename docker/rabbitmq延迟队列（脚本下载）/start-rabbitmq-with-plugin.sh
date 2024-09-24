#!/bin/bash

# 安装 wget
if ! command -v wget &> /dev/null
then
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
if [ ! -f /plugins/rabbitmq_delayed_message_exchange-3.13.0.ez ]; then
    echo "Downloading rabbitmq_delayed_message_exchange plugin..."
    wget -O /plugins/rabbitmq_delayed_message_exchange-3.13.0.ez \
    https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.13.0/rabbitmq_delayed_message_exchange-3.13.0.ez
    if [ $? -ne 0 ]; then
        echo "Failed to download rabbitmq_delayed_message_exchange plugin"
        exit 1
    fi
else
    echo "Plugin already downloaded"
fi

# 修改插件文件权限
echo "Setting permissions for the plugin..."
chmod 644 /plugins/rabbitmq_delayed_message_exchange-3.13.0.ez

# 启用插件
echo "Enabling the plugin..."
rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 启动 RabbitMQ 服务
echo "Starting RabbitMQ server..."
rabbitmq-server
