# Solomon Parent 基础框架

## 📖 项目简介

Solomon Parent 是一个基于 Spring Boot 3.x + JDK 21 的企业级基础框架，集成了微服务开发中常用的各种中间件和工具组件。本项目旨在解决企业开发中遇到的共性问题，提供一套完整的技术解决方案，帮助开发团队快速搭建高质量的企业级应用。

**主要特点:**
- ✅ 基于 Spring Boot 3.4.4 + JDK 21，拥抱最新技术栈
- ✅ 支持多租户 SaaS 架构，灵活切换数据源
- ✅ 完整的国际化 (i18n) 支持，支持动态时区切换
- ✅ 开箱即用的消息队列封装 (RabbitMQ/MQTT/RocketMQ)
- ✅ 统一的对象存储抽象层，支持多种云存储服务
- ✅ 自动化的定时任务管理 (XXL-Job/PowerJob)
- ✅ 完善的全局异常处理和日志记录机制

---

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **JDK** | 21 | Java 开发工具包 |
| **Spring Boot** | 3.4.4 | 应用开发框架 |
| **Spring Cloud Alibaba** | 2023.0.3.2 | 微服务全家桶 |
| **Nacos** | 2.2.2 | 配置中心 & 注册中心 |
| **Sentinel** | 1.8.8 | 流量防卫兵 (限流、熔断降级) |
| **MyBatis-Plus** | 3.5.1 | ORM 持久层框架 |
| **HikariCP** | 6.3.0 | 数据库连接池 |
| **Redis** | - | 缓存数据库 |
| **MongoDB** | - | 分布式文档数据库 |
| **MinIO** | 8.5.17 | 对象存储 |
| **RabbitMQ** | - | 消息队列 |
| **MQTT** | 6.2.2 / 1.2.5 | 物联网消息协议 |
| **RocketMQ** | 2.2.3 | 分布式消息中间件 |
| **XXL-Job** | 3.0.0 | 分布式任务调度平台 |
| **PowerJob** | 5.1.1 | 新一代任务调度框架 |
| **Knife4j** | 4.5.0 | Swagger 增强 UI |
| **Hutool** | 5.8.36 | Java 工具类库 |

---

## 📦 项目结构

```
solomon-parent
├── docker                          # Docker 部署配置文件集
├── solomon-base                    # 基础模块：全局异常处理、Swagger 配置
├── solomon-common                  # 通用模块：AOP 切面、过滤器、Web 配置
├── solomon-constant                # 常量模块：错误编码、缓存定义、Holder 上下文
├── solomon-utils                   # 工具模块：JSON 序列化、文件处理、加密工具等
├── solomon-datasource              # 数据源模块：多租户数据源切换
├── solomon-s3                      # 对象存储模块：统一 S3 协议文件上传下载
├── solomon-redis                   # Redis 模块：多租户 Redis 缓存
├── solomon-mongodb                 # MongoDB 模块：多租户文档数据库
├── solomon-rabbitMq                # RabbitMQ 模块：注解式消息队列
├── solomon-mqtt                    # MQTT 模块 (基于 Spring Integration)
├── solomon-mqtt5                   # MQTT5 模块 (基于 Paho)
├── solomon-vertx-mqtt              # Vert.x MQTT 模块 (响应式)
├── solomon-mica-mqtt               # Mica MQTT 模块 (高性能物联网消息协议)
├── solomon-xxlJob                  # XXL-Job 模块：自动创建任务
├── solomon-powerjob                # PowerJob 模块：自动创建任务
├── solomon-gateway-sentinel        # Gateway 网关 + Sentinel 限流熔断
├── solomon-bot-notice              # 机器人通知模块：钉钉/微信机器人
└── test-*                          # 各模块的测试示例项目
```

---

## 🚀 核心功能

### 1️⃣ 多租户支持

框架支持三种租户模式:
- **NORMAL**: 单库模式，所有租户共享同一数据库
- **SWITCH_DB**: 动态切换数据源模式，根据租户编码切换不同数据库
- **TENANT_PREFIX**: 增加租户前缀模式，在缓存 KEY 前添加租户编码前缀

**切换租户代码示例:**
```java
// 设置租户编码
RequestHeaderHolder.setTenantCode("tenant_001");

// 设置租户 ID
RequestHeaderHolder.setTenantId("123456");

// 设置租户名称
RequestHeaderHolder.setTenantName("测试租户");
```

### 2️⃣ 国际化 (i18n) 支持

#### 配置文件
```yaml
i18n:
  all-locale: zh_CN,en_US  # 支持的语言列表（逗号分隔）
  language: zh_CN          # 默认语言
  path: i18n/messages      # 国际化资源文件路径
```

#### 枚举国际化
1. 实现 `BaseEnum` 接口:
```java
public enum DelFlagEnum implements BaseEnum<String> {
    NOT_DELETE("0", "未删除"),
    DELETE("1", "已删除");

    private final String label;
    private final String desc;

    DelFlagEnum(String label, String desc) {
        this.label = label;
        this.desc = desc;
    }

    @Override
    public String label() { return this.label; }
    
    @Override
    public String key() { return this.name(); }
}
```

2. 实体类字段添加 `@EnumSerialize` 注解:
```java
@EnumSerialize(enumClass = DelFlagEnum.class)
private String delFlag;
```

3. 国际化配置文件 (`messages_zh_CN.properties`):
```properties
DelFlagEnum.NOT_DELETE=未删除
DelFlagEnum.DELETE=已删除
```

### 3️⃣ 时区切换

框架支持根据请求头自动切换时区:

**请求头设置:**
```
Timezone: UTC+8  # 或 GMT+8, Asia/Shanghai
```

**工作原理:**
1. 将传入的时间参数从系统时区转换为目标时区
2. 返回时将时间从系统时区转换回目标时区

### 4️⃣ 全局异常处理

框架提供了完善的全局异常处理机制:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {Throwable.class})
    @ResponseBody
    public Map<String, Object> handleException(
        HttpServletRequest request, 
        HttpServletResponse response, 
        Throwable ex, 
        Locale locale
    ) {
        logger.error("全局异常，请求 ID:{}", ExceptionUtil.requestId.get(), ex);
        return BaseGlobalExceptionHandler.handlerMap(ex, serverId, locale, response);
    }
}
```

**返回格式:**
```json
{
  "httpStatus": 500,
  "errorCode": "S9999",
  "message": "系统内部错误",
  "serverId": "app-001",
  "requestId": "uuid-xxx-xxx"
}
```

### 5️⃣ 对象存储 (S3)

支持多种云存储服务，统一接口:

**配置文件:**
```yaml
file:
  choice: MINIO                                # 存储类型：MINIO/OSS/OBS/COS/BOS/KODO/S3
  endpoint: http://localhost:9000            # 对象存储服务地址
  accessKey: minioadmin                       # 访问密钥
  secretKey: minioadmin                       # 秘密密钥
  bucket-name: default-bucket                 # 默认桶名称
  file-naming-method: UUID                     # 文件命名方式：ORIGINAL/DATE/UUID/SNOWFLAKE
```

**使用示例:**
```java
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileServiceInterface fileService;

    @PostMapping("/upload")
    public ResultVO<FileUpload> upload(@RequestPart("file") MultipartFile file) throws Exception {
        // 普通上传
        FileUpload upload = fileService.upload(file, "bucket-name");
        
        // 分片上传
        FileUpload multipartUpload = fileService.multipartUpload(file, "bucket-name");
        
        // 分享文件
        String shareUrl = fileService.share("filename.txt", "bucket-name", 3600, TimeUnit.SECONDS);
        
        // 删除文件
        fileService.deleteFile("filename.txt", "bucket-name");
        
        return ResultVO.success(upload);
    }
}
```

### 6️⃣ 消息队列

#### RabbitMQ
**配置:**
```yaml
spring:
  rabbitmq:
    username: guest                    # RabbitMQ 用户名
    password: guest                    # RabbitMQ 密码
    host: localhost                    # RabbitMQ 主机地址
    port: 5672                         # RabbitMQ 端口
    auto-delete-queue: true            # 是否自动删除队列
    auto-delete-exchange: true         # 是否自动删除交换机
```

**消费者示例:**
```java
@MessageListener(queues = "test_queue", exchange = "test_exchange")
@MessageListenerRetry(retryNumber = 3)  // 重试 3 次
public class TestConsumer extends AbstractConsumer<String, String> {

    @Override
    public String handleMessage(String body) throws Exception {
        logger.info("收到消息：{}", body);
        return "success";
    }

    @Override
    public void saveFailMessage(Message message, Exception e) {
        logger.error("消息处理失败", e);
    }
}
```

**死信队列:**
```java
@MessageListener(
    queues = "main_queue", 
    exchange = "main_exchange",
    dlxClazz = TestDlxConsumer.class  // 指定死信队列处理器
)
public class TestConsumer extends AbstractConsumer<String, String> {
    // ...
}

@DlxMessageListener
public class TestDlxConsumer extends AbstractConsumer<String, String> {
    @Override
    public String handleMessage(String body) throws Exception {
        logger.warn("死信消息：{}", body);
        return "processed";
    }
}
```

#### MQTT
**配置:**
```yaml
mqtt:
  tenant:
    default:
      user-name: admin                 # MQTT 用户名
      password: password               # MQTT 密码
      url: tcp://localhost:1883        # Broker 地址（支持 tcp/ssl/ws/wss）
      client-id: client-001            # 客户端 ID（唯一标识）
      clean-session: true               # 是否清除会话
      keep-alive-interval: 60          # 心跳间隔（秒）
```

**消费者示例:**
```java
@MessageListener(topics = "device/topic", qos = 2)
public class MqttConsumer extends AbstractConsumer<String> {

    @Override
    public void handleMessage(String body) throws Exception {
        logger.info("MQTT 消息：{}", body);
    }

    @Override
    public void saveFailMessage(String topic, MqttMessage message, Exception e) {
        logger.error("MQTT 消息处理失败", e);
    }
}
```

### 7️⃣ Mica MQTT 消息协议

**配置文件:**
```yaml
mqtt:
  enabled: true                         # 是否启用 Mica MQTT
  tenant:
    default:
      name: client-name                 # 客户端名称
      username: admin                   # MQTT 用户名
      password: password               # MQTT 密码
      ip: tcp://localhost:1883         # Broker 地址
      client-id: client-001            # 客户端 ID（唯一）
      clean-session: true               # 断开后清除会话
      keep-alive-interval: 60          # 心跳间隔（秒）
      timeout: 30                       # 连接超时（秒）
```

**消费者示例:**
```java
@MessageListener(topics = "device/+/data", qos = 2)
public class DeviceMqttConsumer extends AbstractConsumer {

    @Override
    public void consume(MqttModel mqttModel) throws Exception {
        logger.info("收到 Mica MQTT 消息 - Topic: {}, Payload: {}", 
            mqttModel.getTopic(), mqttModel.getPayload());
        
        // 处理消息逻辑
        String payload = mqttModel.getPayload();
        // ...
    }
}
```

**发送消息:**
```java
// 发送消息到指定主题
MqttUtils.publish("device/command", "turn_on");

// 指定 QoS 发送
MqttUtils.publish("device/command", "turn_on", 1);

// 指定租户发送
MqttUtils.publish("tenant_001", "device/command", "turn_on", 2);
```

**主要特点:**
- ✅ 基于 Mica MQTT 高性能客户端
- ✅ 注解式消息监听，支持通配符主题
- ✅ 多租户支持，每个租户独立连接
- ✅ 自动重连机制
- ✅ QoS 0/1/2 消息质量支持
- ✅ 遗嘱消息配置

---

### 8️⃣ Vert.x MQTT 消息协议（响应式）

**配置文件:**
```yaml
mqtt:
  enabled: true                         # 是否启用 Vert.x MQTT
  tenant:
    default:
      user-name: admin                  # MQTT 用户名
      password: password               # MQTT 密码
      url: tcp://localhost:1883        # Broker 地址
      client-id: vertx-client-001      # 客户端 ID（唯一）
      completion-timeout: 30000        # 连接超时（毫秒）
      automatic-reconnect: true        # 是否自动重连
      clean-session: false             # 掉线后是否清除会话
      keep-alive-interval: 60         # 心跳间隔（秒）
      max-inflight: 10                 # 最大未确认消息数
      reconnect-attempts: -1           # 重连次数（-1 无限重连，0 不重连）
      reconnect-interval: 1000         # 重连间隔（毫秒）
      verify-certificate: false        # SSL 连接是否验证证书
      # 遗嘱消息配置
      will:
        topic: device/status            # 遗嘱主题
        message: offline                # 遗嘱消息内容
        qos: 1                          # 遗嘱消息 QoS
        retained: false                 # 是否保留消息
      # Vert.x 线程池配置
      vertx:
        event-loop-pool-size: 8         # 事件循环线程池大小
        worker-pool-size: 20            # Worker 线程池大小
        max-event-loop-execute-time: 2  # 事件循环最大执行时间（秒）
```

**消费者示例:**
```java
@MessageListener(topics = "sensor/#", qos = 1)
public class SensorMqttConsumer extends AbstractConsumer {

    @Override
    public void consume(MqttModel mqttModel) throws Exception {
        logger.info("收到 Vert.x MQTT 消息");
        logger.info("  - Topic: {}", mqttModel.getTopic());
        logger.info("  - Payload: {}", mqttModel.getPayload());
        logger.info("  - QoS: {}", mqttModel.getQos());
        logger.info("  - Retained: {}", mqttModel.isRetained());
        
        // 处理消息逻辑
        // ...
    }
}
```

**发送消息:**
```java
// 发送消息到指定主题
MqttUtils.publish("device/command", "{\"action\":\"restart\"}");

// 指定 QoS 发送
MqttUtils.publish("device/command", "payload", 2);

// 指定租户发送
MqttUtils.publish("tenant_001", "device/command", "payload", 1);
```

**主要特点:**
- ✅ 基于 Vert.x 响应式编程模型，高并发性能优秀
- ✅ 注解式消息监听，支持通配符主题（+/#）
- ✅ 多租户支持，每个租户独立连接
- ✅ 完整的遗嘱消息配置
- ✅ QoS 0/1/2 消息质量支持
- ✅ 可配置的自动重连策略
- ✅ 可自定义 Vert.x 线程池参数

---

### 9️⃣ 定时任务

#### XXL-Job 自动创建任务
**配置:**
```yaml
xxl:
  admin-addresses: http://localhost:8080/xxl-job-admin  # XXL-Job 管理后台地址
  access-token: default_token                              # 访问令牌
  app-name: solomon-executor                               # 执行器名称
  enabled: true                                            # 是否启用
```

**任务示例:**
```java
@JobTask(
    taskName = "测试任务",
    author = "steven",
    executorHandler = "TestJobHandler",
    scheduleType = ScheduleTypeEnum.FIX_RATE,
    scheduleConf = "30000",  // 30 秒执行一次
    start = true
)
public class TestJob extends AbstractJobConsumer {

    @Override
    public void handle(String jobParam) {
        logger.info("执行任务，参数：{}", jobParam);
    }

    @Override
    public void saveLog(Throwable throwable) {
        logger.error("任务执行失败", throwable);
    }
}
```

#### PowerJob 自动创建任务
**配置:**
```yaml
powerjob:
  worker:
    enabled: true                    # 是否启用 PowerJob Worker
    port: 27777                      # Worker 端口
    app-name: solomon                # 应用名称
    server-address: localhost:7700   # Server 地址
    protocol: http                   # 通信协议
```

**任务示例:**
```java
@JobTask(taskName = "PowerJob 测试任务")
public class TestJob implements BasicProcessor {

    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        logger.info("PowerJob 任务执行");
        return new ProcessResult(true, "成功");
    }
}
```

### 1️⃣0️⃣ Redis 缓存

**单机版配置:**
```yaml
spring:
  cache:
    mode: NORMAL                       # 缓存模式：NORMAL 单库，SWITCH_DB 多租户切换
    type: REDIS                        # 缓存类型
  redis:
    host: localhost                    # Redis 主机地址
    port: 6379                         # Redis 端口
    database: 0                        # 数据库编号
```

**多租户配置:**
```yaml
spring:
  cache:
    mode: SWITCH_DB                   # 缓存模式：NORMAL 单库，SWITCH_DB 多租户切换
    type: REDIS                       # 缓存类型
  redis:
    tenant:
      tenant_001:
        host: localhost               # 租户 001 Redis 地址
        port: 6379                    # 租户 001 Redis 端口
        database: 0                    # 租户 001 数据库编号
      tenant_002:
        host: localhost               # 租户 002 Redis 地址
        port: 6380                    # 租户 002 Redis 端口
        database: 0                    # 租户 002 数据库编号
```

### 1️⃣1️⃣ MongoDB

**多租户配置:**
```yaml
spring:
  data:
    mongodb:
      mode: SWITCH_DB                  # 模式：NORMAL 单库，SWITCH_DB 多租户切换
      tenant:
        tenant_001:
          uri: mongodb://user:pass@localhost:27017/db1  # 租户 001 连接 URI
        tenant_002:
          uri: mongodb://user:pass@localhost:27017/db2  # 租户 002 连接 URI
```

**固定集合配置:**
```java
@MongoDBCapped(size = 2048, maxDocuments = 500000)
@Document(collection = "logs")
public class LogEntity {
    @Id
    private String id;
    private Date created;
    private String content;
}
```

### 1️⃣2️⃣ 病毒扫描 (ClamAV)

**配置:**
```yaml
clamav:
  enabled: true                    # 是否启用 ClamAV 病毒扫描
  host: localhost                  # ClamAV 主机地址
  port: 3310                       # ClamAV 端口
  platform: unix                   # 平台类型：unix/windows
```

**使用示例:**
```java
@PostMapping("/upload")
public ResultVO<String> upload(@RequestPart("file") MultipartFile file) throws Exception {
    // 扫描病毒
    clamAvUtils.scanFile(file.getInputStream(), "FILE_HIGH_RISK");
    
    // 上传文件
    FileUpload upload = fileService.upload(file, "bucket");
    
    return ResultVO.success(upload);
}
```

### 1️⃣3️⃣ 机器人通知

支持钉钉、微信机器人发送通知:

```java
// 发送钉钉消息
BotNoteUtils.sendDingTalkNote("webhook_url", "标题", "内容");

// 发送微信消息
BotNoteUtils.sendWeChatNote("webhook_url", "标题", "内容");
```

---

## 🐳 Docker 部署

项目 `docker` 目录包含以下组件的 Docker Compose 配置文件:

| 组件 | 说明 |
|------|------|
| `nacos/` | Nacos 配置中心 (支持集群) |
| `redis/` | Redis 缓存 (支持主从/哨兵集群) |
| `mysql/` | MySQL 数据库 |
| `mongodb/` | MongoDB 文档数据库 |
| `minio/` | MinIO 对象存储 |
| `rabbitmq/` | RabbitMQ 消息队列 (支持延迟队列) |
| `emqx/` | EMQX MQTT 服务器 |
| `rocketmq/` | RocketMQ 消息队列 |
| `elasticsearch/` | Elasticsearch 搜索引擎 |
| `sonarqube/` | SonarQube 代码质量检查 |
| `jenkins/` | Jenkins CI/CD |
| `portainer/` | Docker 可视化管理 |
| `gitea/` | Git 代码仓库 |
| `nexus/` | Maven 私有仓库 |
| `xxl-job/` | XXL-Job 任务调度 |
| `powerjob/` | PowerJob 任务调度 |
| `sentinel/` | Sentinel 流量控制 |
| `kafka/` | Kafka 消息队列 |
| `zookeeper/` | Zookeeper 协调服务 |
| `nginx/` | Nginx 反向代理 |
| `postgres/` | PostgreSQL 数据库 |
| `mariadb/` | MariaDB 数据库 |
| `clamav/` | ClamAV 病毒扫描 |

**启动示例:**
```bash
# 启动 Nacos
cd docker/nacos
docker-compose up -d

# 启动 Redis 哨兵集群
cd docker/redis 哨兵主从集群
docker-compose up -d
```

---

## 📝 Swagger API 文档

**配置:**
```yaml
doc:
  title: Solomon API 文档                      # 文档标题
  enabled: true                                # 是否启用 Swagger 文档
  globalRequestParameters:                     # 全局请求参数
    - name: Authorization
      in: HEADER
      description: JWT Token
      required: true
      deprecated: false
      hidden: false
```

**访问地址:** `http://localhost:8080/doc.html`

**获取 Git 版本号:**
需要在 `pom.xml` 中添加插件:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>pl.project13.maven</groupId>
            <artifactId>git-commit-id-plugin</artifactId>
            <version>2.1.5</version>
            <executions>
                <execution>
                    <phase>initialize</phase>
                    <goals>
                        <goal>revision</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <generateGitPropertiesFile>true</generateGitPropertiesFile>
                <generateGitPropertiesFilename>/src/main/resources/git.properties</generateGitPropertiesFilename>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 🔧 工具类

### JSON 序列化配置

框架内置了完善的 JSON 序列化配置:

- **Long 类型转字符串**: 防止 JavaScript 精度丢失
- **日期格式化**: 统一格式 `yyyy-MM-dd HH:mm:ss.SSS`
- **时区适配**: 支持动态时区切换

### 日期工具类

```java
// 获取当前时间字符串
String now = DateTimeUtils.getLocalDateTimeString();

// 日期格式转换
LocalDateTime dateTime = DateTimeUtils.string2LocalDateTime("2024-01-01 12:00:00");

// 时区转换
LocalDateTime targetTime = DateTimeUtils.convertLocalDateTime(
    sourceTime, 
    ZoneId.of("Asia/Shanghai"), 
    ZoneId.of("UTC")
);

// 获取月初月末
LocalDate firstDay = DateTimeUtils.getNowMonthFirstDayTime();
LocalDate lastDay = DateTimeUtils.getNowMonthLaseDayTime();
```

### 排序工具类

```java
// 多字段排序
SortUtil.sort(
    SortTypeEnum.QUICK_SORT, 
    list, 
    Comparator.comparing(Person::getAge)
              .thenComparing(Person::getName)
              .reversed()
);
```

---

## ⚠️ 注意事项

### ThreadLocal 使用警告

框架使用了 ThreadLocal 来存储请求上下文信息，请注意:

1. **内存泄漏风险**: ThreadLocal 必须手动清理，否则在使用线程池的场景下会导致内存泄漏
2. **清理建议**: 在过滤器或拦截器的 `finally` 块中调用 `remove()` 方法

### 敏感信息保护

生产环境建议对日志进行脱敏处理，避免泄露:
- 密码、Token
- 身份证号、手机号
- 银行卡号等敏感信息

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request!

**开发者群:**
![微信群二维码](img.png)

---

## 📄 许可证

[Apache License 2.0](LICENSE)

---

## 📧 联系方式

- **作者**: steven
- **邮箱**: cao136623@163.com

---

## 🎯 最佳实践

1. **合理使用多租户模式**: 根据业务场景选择合适的租户隔离策略
2. **规范使用注解**: 充分利用框架提供的注解简化开发
3. **异常处理**: 统一使用框架的全局异常处理机制
4. **日志规范**: 遵循 SLF4J 日志规范，合理分级
5. **性能优化**: 对于高频接口，考虑关闭 AOP 日志记录

---

## 🔄 更新日志

### v1.0 (当前版本)
- ✅ 基于 Spring Boot 3.4.4 + JDK 21
- ✅ 集成主流中间件和工具
- ✅ 完善的多租户支持
- ✅ 强大的对象存储抽象层
- ✅ 自动化任务调度
- ✅ 完整的国际化支持
