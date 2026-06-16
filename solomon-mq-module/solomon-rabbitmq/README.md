# solomon-rabbitmq

基于 Spring AMQP 的 RabbitMQ 消息模块，复用 `solomon-mq-sdk` 的统一消费模板与多租户路由能力。

## 特性

- 统一消费模板：继承 `AbstractConsumer` 即可获得消息反序列化、幂等校验、手动 ACK、失败重试、消费日志。
- 多租户支持：通过 `rabbitmq.tenant-mode` 在「每租户独立连接」和「共享连接按租户编码路由」之间切换。
- 六种交换机类型：Direct、Topic、Fanout、Headers、Delayed（延时）发送服务开箱即用。
- 请求-回应（RPC）：消费者可自动将处理结果回发到 `replyTo` 队列。
- 失败重试：通过 `@MessageListenerRetry` 配置重试次数，达到上限后拒绝消息。

## 引入

```xml
<dependency>
    <groupId>com.steven</groupId>
    <artifactId>solomon-mq-sdk</artifactId>
</dependency>
<dependency>
    <groupId>com.steven</groupId>
    <artifactId>solomon-rabbitmq</artifactId>
</dependency>
```

## 单租户配置

沿用 Spring Boot 标准配置：

```yaml
spring:
  rabbitmq:
    enabled: true
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
    virtual-host: /
```

## 多租户配置

通过 `rabbitmq.tenant-mode` 切换两种连接模式：

| 模式 | 行为 | 适用场景 |
| --- | --- | --- |
| `PER_TENANT_CONNECTION`（默认） | 为每个租户创建独立连接工厂 | 各租户连接不同 RabbitMQ，需物理隔离 |
| `SHARED_CONNECTION` | 共享一个连接，按消息体 tenantCode 路由 | 所有租户共用同一 RabbitMQ，仅逻辑隔离 |

```yaml
rabbitmq:
  tenant-mode: PER_TENANT_CONNECTION   # 或 SHARED_CONNECTION
  tenant:
    default:
      host: 127.0.0.1
      port: 5672
      username: guest
      password: guest
      virtual-host: /default
    tenant-a:
      host: 10.0.0.2
      port: 5672
      username: admin
      password: secret
      virtual-host: /tenantA
```

> 每个租户的配置类型为 Spring Boot 原生 `RabbitProperties`，可使用 `host/port/username/password/virtual-host` 等全部标准字段。

## 消费者开发

继承 `AbstractConsumer` 并用 `@MessageListener` 声明队列与交换机：

```java
@MessageListener(
    queue = "order.queue",
    exchange = "order.exchange",
    routingKey = "order.*",
    type = ExchangeType.DIRECT,
    mode = AcknowledgeMode.MANUAL
)
public class OrderConsumer extends AbstractConsumer<OrderEvent, String> {

    @Override
    public String handleMessage(OrderEvent body) throws Exception {
        // 处理订单事件
        return "处理成功";
    }

    @Override
    public void saveLog(String result, Throwable throwable, RabbitMqModel<OrderEvent> model) {
        // 记录消费日志
    }
}
```

### 失败重试

```java
@MessageListenerRetry(retryNumber = 3)
@MessageListener(queue = "retry.queue", ...)
public class RetryConsumer extends AbstractConsumer<...> { ... }
```

重试次数达到上限后，消息会被拒绝（`basicNack` 且不重新入队）。

## 发送消息

注入 `SendService` 或 `RabbitUtils` 发送消息：

```java
@Autowired
private SendService<RabbitMqModel<OrderEvent>> sendService;

public void sendOrder(OrderEvent event) throws Exception {
    RabbitMqModel<OrderEvent> message = new RabbitMqModel<>("order.exchange", "order.create", event);
    message.setTenantCode("tenant-a");
    sendService.send(message);
}
```

支持即时发送（`send`）、延时发送（`sendDelay`）和过期发送（`sendExpiration`）。

## 设计说明

| 组件 | 职责 |
| --- | --- |
| `AbstractConsumer` | 消费者基类，实现 `MessageListenerSpi`，统一消费流程 |
| `RabbitMqModel` | 消息模型，继承 `BaseMq`，携带租户编码、交换机、路由键 |
| `RabbitMqTenantContext` | 多租户连接上下文，按租户切换连接工厂 |
| `RabbitMqTenantInitService` | 多租户连接初始化，按 `tenant-mode` 创建连接 |
| `AbstractMQService` | 交换机绑定模板，六种子类型对应六种交换机 |
