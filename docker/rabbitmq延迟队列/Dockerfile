# 使用基础镜像 RabbitMQ 管理版
FROM rabbitmq:management

# 添加延迟消息插件到指定目录 可按照此方式自行扩展其他插件
RUN apt-get update && apt-get install -y curl && \
    mkdir -p /plugins && \
    curl -L -o /plugins/rabbitmq_delayed_message_exchange-3.13.0.ez \
    https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.13.0/rabbitmq_delayed_message_exchange-3.13.0.ez

# 修改插件目录权限
RUN chmod 644 /plugins/rabbitmq_delayed_message_exchange-3.13.0.ez

# 开启插件
RUN rabbitmq-plugins enable rabbitmq_delayed_message_exchange

ENTRYPOINT ["rabbitmq-server"]