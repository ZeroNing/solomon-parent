#!/usr/bin/env bash

# 下载与 RabbitMQ 3.13 配套的延迟消息插件，离线启用后启动服务。
set -euo pipefail

plugin_version="3.13.0"
plugin_file="/plugins/rabbitmq_delayed_message_exchange-${plugin_version}.ez"
plugin_url="https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v${plugin_version}/rabbitmq_delayed_message_exchange-${plugin_version}.ez"

if [[ ! -f "${plugin_file}" ]]; then
  apt-get update
  apt-get install -y --no-install-recommends curl ca-certificates
  curl --fail --location --retry 3 --output "${plugin_file}" "${plugin_url}"
  rm -rf /var/lib/apt/lists/*
fi

chmod 0644 "${plugin_file}"
rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange
exec docker-entrypoint.sh rabbitmq-server
