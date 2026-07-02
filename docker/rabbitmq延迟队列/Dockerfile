# RabbitMQ 3.13 与 3.13.0 版延迟消息插件配套使用，避免主版本不兼容。
FROM rabbitmq:3.13-management

ARG DELAYED_PLUGIN_VERSION=3.13.0

# 构建时下载并离线启用插件；清理 apt 缓存以减小镜像体积。
# 基础镜像的软件源会随补丁版本变化，此处不固定 apt 包版本，避免镜像源找不到指定补丁。
# hadolint ignore=DL3008
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && curl --fail --location --retry 3 \
      --output "/plugins/rabbitmq_delayed_message_exchange-${DELAYED_PLUGIN_VERSION}.ez" \
      "https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v${DELAYED_PLUGIN_VERSION}/rabbitmq_delayed_message_exchange-${DELAYED_PLUGIN_VERSION}.ez" \
    && chmod 0644 "/plugins/rabbitmq_delayed_message_exchange-${DELAYED_PLUGIN_VERSION}.ez" \
    && rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange \
    && rm -rf /var/lib/apt/lists/*
