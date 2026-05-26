# Solomon Mica-MQTT 模块

基于 Mica-MQTT 的 MQTT 客户端封装模块，提供多租户支持的 MQTT 消息收发功能。

## 功能特性

- ✅ 基于 Mica-MQTT 2.5.9 版本
- ✅ 支持多租户配置
- ✅ 自动重连机制
- ✅ 遗嘱消息支持
- ✅ 注解式消息订阅（@MessageListener）
- ✅ 服务质量（QoS）支持
- ✅ SSL/TLS 加密连接

## 快速开始

### 1. 添加依赖

在项目的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.steven</groupId>
    <artifactId>solomon-mica-mqtt</artifactId>
    <version>1.0</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中配置 MQTT：

```yaml
mqtt:
  enabled: true  # 是否启用MQTT
  tenant:
    tenant1:  # 租户编码
      url: tcp://localhost:1883  # MQTT服务器地址
      userName: admin  # 用户名
      password: password  # 密码
      clientId: client_001  # 客户端ID（可选，不填自动生成UUID）
      keepAliveInterval: 60  # 心跳间隔（秒）
      connectionTimeout: 30  # 连接超时（秒）
      automaticReconnect: true  # 自动重连
      verifyCertificate: false  # 是否验证证书
      will:  # 遗嘱消息（可选）
        topic: /will/topic
        message: offline
        qos: 1
        retained: false
```

### 3. 创建消息消费者

使用 `@MessageListener` 注解订阅主题：

```java
import com.steven.solomon.mqtt.annotation.MessageListener;
import com.steven.solomon.consumer.AbstractConsumer;
import org.springframework.stereotype.Component;

@Component
@MessageListener(topics = {"/test/topic"}, qos = 1)
public class TestConsumer extends AbstractConsumer<String, Void> {
    
    @Override
    protected Void consume(String data) {
        System.out.println("收到消息: " + data);
        // 处理业务逻辑
        return null;
    }
}
```

### 4. 发送消息

注入 `MqttUtils` 发送消息：

```java
import com.steven.solomon.entity.MqttModel;
import com.steven.solomon.utils.MqttUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    
    @Autowired
    private MqttUtils mqttUtils;
    
    public void sendMessage() throws Exception {
        MqttModel<String> model = new MqttModel<>();
        model.setTenantCode("tenant1");
        model.setTopic("/test/topic");
        model.setPayload("Hello MQTT!");
        model.setQos(1);
        model.setRetained(false);
        
        mqttUtils.send(model);
    }
}
```

## API 说明

### MqttUtils 主要方法

| 方法 | 说明 |
|------|------|
| `send(MqttModel<?> data)` | 发送消息 |
| `subscribe(String tenantCode, String topic, int qos, AbstractConsumer<?, ?> consumer)` | 订阅主题 |
| `unsubscribe(String tenantCode, String[] topic)` | 取消订阅 |
| `disconnect(String tenantCode)` | 断开连接 |
| `reconnect(String tenantCode)` | 重新连接 |

### @MessageListener 注解属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| topics | String[] | - | 订阅的主题列表 |
| qos | int | 0 | 服务质量（0, 1, 2） |
| tenantRange | String[] | {} | 允许订阅的租户范围 |

## 配置项说明

### MqttProfile 配置项

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| url | String | - | MQTT服务器地址（支持多个，逗号分隔） |
| userName | String | - | 用户名 |
| password | String | - | 密码 |
| clientId | String | UUID | 客户端ID |
| keepAliveInterval | int | 60 | 心跳间隔（秒） |
| connectionTimeout | int | 30 | 连接超时（秒） |
| automaticReconnect | boolean | true | 是否自动重连 |
| verifyCertificate | boolean | false | 是否验证证书 |
| will | MqttWill | null | 遗嘱消息配置 |

### MqttWill 遗嘱消息配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| topic | String | - | 遗嘱主题 |
| message | String | - | 遗嘱消息内容 |
| qos | int | 0 | 服务质量 |
| retained | boolean | false | 是否保留消息 |

## 注意事项

1. **多租户支持**：每个租户可以有独立的 MQTT 配置和客户端实例
2. **自动重连**：默认启用自动重连，断线后会自动尝试重连
3. **主题订阅**：使用 `@MessageListener` 注解的类会被自动扫描并订阅相应主题
4. **消息格式**：消息 payload 会自动序列化为 JSON 格式
5. **SSL/TLS**：设置 `verifyCertificate: false` 可跳过证书验证（仅测试环境）

## 与其他 MQTT 模块的区别

| 模块 | 底层实现 | 特点 |
|------|----------|------|
| solomon-mqtt | Eclipse Paho MQTT v3 | 经典稳定，广泛使用 |
| solomon-mqtt5 | Eclipse Paho MQTT v5 | 支持 MQTT 5.0 新特性 |
| solomon-vertx-mqtt | Vert.x MQTT | 响应式编程模型 |
| **solomon-mica-mqtt** | **Mica-MQTT** | **高性能，国产开源，Spring Boot 友好** |

## 许可证

Apache License 2.0

## 作者

steven (cao136623@163.com)
