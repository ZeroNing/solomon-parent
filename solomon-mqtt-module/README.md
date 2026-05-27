# Solomon MQTT Module

`solomon-mqtt-module` 是 MQTT 聚合模块，采用公共 SDK + 具体客户端实现的结构。业务项目只需要引入 `solomon-mqtt-sdk` 和一个实现模块，避免多个 MQTT 客户端 SDK 同时进入运行时。

## 模块

| 模块 | 说明 |
| --- | --- |
| `solomon-mqtt-sdk` | 公共注解、消息模型、消费者模板、租户初始化模板、客户端注册表、监听器解析、SSL 工具 |
| `solomon-mqtt` | Paho MQTT 3 / Spring Integration MQTT 实现 |
| `solomon-mqtt5` | Eclipse Paho MQTT 5 实现 |
| `solomon-vertx-mqtt` | Vert.x MQTT 实现 |
| `solomon-mica-mqtt` | Mica MQTT 实现 |
| `solomon-redis-mqtt` | Redis Pub/Sub MQTT 风格实现 |

## 依赖

只选择一个实现模块。多个实现同时引入时，自动配置类包名相同，容易造成类路径覆盖。

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-mqtt-sdk</artifactId>
  <version>1.0</version>
</dependency>

<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-mqtt</artifactId>
  <version>1.0</version>
</dependency>
```

Redis 实现使用 Redis Pub/Sub，不需要 MQTT Broker：

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-mqtt-sdk</artifactId>
  <version>1.0</version>
</dependency>

<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-redis-mqtt</artifactId>
  <version>1.0</version>
</dependency>
```

## 多租户配置

```yaml
mqtt:
  enabled: true
  tenant:
    default:
      url: tcp://127.0.0.1:1883
      client-id: demo-service
      user-name: admin
      password: 123456
      clean-session: true
      automatic-reconnect: true
      keep-alive-interval: 20
      verify-certificate: true

    redis-local:
      host: 127.0.0.1
      port: 6379
      database: 0
      username:
      password:
      ssl: false
      timeout: 60000
      channel-prefix: demo
      use-tenant-prefix: true
      pattern-topic: false
```

`solomon-redis-mqtt` 的 `mqtt.tenant.*` 就是多租户 Redis 配置，每个租户会通过 `solomon-cache-redis` 的连接工厂构建器创建独立 Redis 连接、监听容器和发送模板。不同租户可以连接不同 Redis 实例，也可以连接同一实例的不同 database。

Redis MQTT 会把连接注册到 `RedisCacheTenantContext`，租户编码使用 `mqtt:{tenantCode}` 命名空间，避免覆盖普通缓存租户连接。

## SSL / TLS

四个实现都支持 SSL：

- `solomon-mqtt` 和 `solomon-mqtt5`：把 `url` 配成 `ssl://host:8883` 即可；`verify-certificate=false` 时会使用 SDK 的 `MqttSslFactory` 信任所有证书。
- `solomon-vertx-mqtt`：把 `url` 配成 `ssl://host:8883` 会自动开启 SSL；未写端口时默认使用 `8883`。
- `solomon-mica-mqtt`：优先读取 Mica 自身的 `ssl.enabled` 和证书配置；如果端口是 `8883`，也会自动启用 `useSsl()`。
- `solomon-redis-mqtt`：不涉及 MQTT SSL；Redis SSL 通过当前租户的 `ssl=true` 开启，底层连接创建复用 `solomon-cache-redis`。

```yaml
mqtt:
  tenant:
    default:
      url: ssl://broker.example.com:8883
      verify-certificate: true
```

## 监听消息

```java
@MessageListener(
    description = "设备状态上报",
    topics = {"device/+/status"},
    qos = 1,
    tenantRange = {"default"}
)
public class DeviceStatusConsumer extends AbstractConsumer<DeviceStatusMessage, Void> {

  @Override
  public Void handleMessage(DeviceStatusMessage body) {
    // 中文注释：这里只处理业务消息，租户上下文和重复消费检查由 SDK 公共模板完成。
    return null;
  }
}
```

Redis MQTT 为避免和 `solomon-redis` 的消费者基类重名，消费者继承独立基类：

```java
@MessageListener(topics = {"device/+/status"}, qos = 1, tenantRange = {"redis-local"})
public class RedisDeviceStatusConsumer extends AbstractRedisMqttConsumer<DeviceStatusMessage, Void> {

  @Override
  public Void handleMessage(DeviceStatusMessage body) {
    return null;
  }
}
```

`@MessageListener` 支持 `enabled=false` 临时关闭监听器。主题支持 Spring 占位符表达式，SDK 会统一解析后再交给具体实现订阅。

## 发送消息

```java
MqttMessageModel<DeviceCommand> message = new MqttMessageModel<>();
message.setTenantCode("default");
message.setTopic("device/1001/command");
message.setQos(1);
message.setRetained(false);
message.setBody(command);

sendService.send(message);
```

发送失败会抛出底层异常并记录 `tenant/topic/payload`，业务侧可以明确感知失败。

## 设计约定

- 客户端和连接参数统一由 `AbstractMqttClientRegistry` 管理，内部使用并发 Map，支持并发发送、重连和注销。
- 监听器扫描统一由 `MqttListenerRegistry` 完成，具体实现只负责把订阅描述转换成底层 SDK 调用。
- 消费者公共模板 `AbstractMqttConsumerSupport` 会在消费结束后清理 `RequestHeaderHolder`，避免线程池复用导致租户上下文串用。
- 关闭证书校验时统一使用 `MqttSslFactory` 创建信任所有证书的 SocketFactory；生产环境建议保持 `verify-certificate=true`。
- Vert.x MQTT 按租户维护消费者映射，避免多个租户订阅相同 topic 时串消费者。
- Redis MQTT 将 MQTT topic 映射为 Redis channel；`+` 和 `#` 会转换成 Redis PatternTopic 的 `*`。

## 构建

```bash
mvn -pl solomon-mqtt-module -am clean compile
```
