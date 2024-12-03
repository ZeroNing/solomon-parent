#!/bin/bash

# 启动 RabbitMQ 服务
docker-entrypoint.sh rabbitmq-server &

# 等待 RabbitMQ 启动
echo "等待 RabbitMQ 服务启动"
sleep 20  # 等待时间可以根据实际情况调整

# 启用延迟消息插件
echo "启用延迟消息插件"
rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 保持前台运行，避免容器退出
wait
