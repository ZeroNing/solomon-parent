#!/usr/bin/env bash

# 在 RabbitMQ 启动前离线启用仓库内置的延迟消息插件。
set -euo pipefail

rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange
exec docker-entrypoint.sh rabbitmq-server
