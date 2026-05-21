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
├── solomon-epc-coder               # GS1 EPC 编解码模块：SGTIN/GIAI 生成与反译
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

#### Mica MQTT 消息协议

**配置文件:**
```yaml
mqtt:
  enabled: true                         # 是否启用 Mica MQTT
  tenant:
    default:
      name: client-name                 # 客户端名称
      username: admin                   # MQTT 用户名
      password: password               # MQTT 密码
      ip: 127.0.0.1                    # Broker IP 地址
      port: 1883                       # Broker 端口
      client-id: client-001            # 客户端 ID（唯一标识）
      keep-alive-secs: 60              # 心跳间隔（秒）
      timeout: 30                       # 连接超时（秒）
      clean-start: true                 # 干净启动
      reconnect: true                   # 是否自动重连
      re-interval: 1000                # 重连间隔（毫秒）
      retry-count: -1                   # 重试次数（-1 无限重试）
      biz-thread-pool-size: 8           # 业务线程池大小
      debug: false                      # 调试模式
      # 遗嘱消息配置
      will-message:
        topic: device/status            # 遗嘱主题
        message: offline                # 遗嘱消息内容
        qos: 1                          # 遗嘱消息 QoS
        retain: false                   # 是否保留消息
```

**消费者示例:**
```java
@MessageListener(topics = "device/+/data", qos = 2)
public class DeviceMqttConsumer extends AbstractConsumer<String, String> {

    @Override
    public String handleMessage(String body) throws Exception {
        logger.info("收到 Mica MQTT 消息 - Payload: {}", body);
        
        // 处理消息逻辑
        // body 是消息内容（JSON 字符串或普通字符串）
        // MqttModel 包含 tenantCode、topic、qos、retained 等信息
        
        // 返回处理结果（如果不需要返回结果，返回 null 即可）
        return "success";
    }

    @Override
    public void saveLog(String result, Throwable throwable, MqttModel<String> model) {
        // 保存消费日志或记录消费结果
        if (throwable != null) {
            logger.error("消息消费失败，topic: {}", model.getTopic(), throwable);
        } else {
            logger.info("消息消费成功，topic: {}, result: {}", model.getTopic(), result);
        }
    }
}
```

**发送消息:**
```java
@Resource
private MqttUtils mqttUtils;

// 发送消息到指定主题
MqttModel<String> model = new MqttModel<>("default", "device/command", "turn_on");
model.setQos(1);                     // 设置 QoS
model.setRetained(false);            // 是否保留消息
mqttUtils.send(model);

// 指定租户发送
MqttModel<String> model = new MqttModel<>("tenant_001", "device/command", "turn_on");
model.setQos(2);
mqttUtils.send(model);
```

**主要特点:**
- ✅ 基于 Mica MQTT 高性能客户端
- ✅ 注解式消息监听，支持通配符主题
- ✅ 多租户支持，每个租户独立连接
- ✅ 自动重连机制
- ✅ QoS 0/1/2 消息质量支持
- ✅ 遗嘱消息配置

---

#### Vert.x MQTT 消息协议（响应式）

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
public class SensorMqttConsumer extends AbstractConsumer<String, String> {

    @Override
    public String handleMessage(String body) throws Exception {
        logger.info("收到 Vert.x MQTT 消息 - Payload: {}", body);
        logger.info("  - 当前 Topic: {}", this.topic);
        logger.info("  - 租户编码: {}", this.tenantCode);
        
        // 处理消息逻辑
        // body 是消息内容（JSON 字符串或普通字符串）
        
        // 返回处理结果（如果不需要返回结果，返回 null 即可）
        return "processed";
    }

    @Override
    public void saveLog(String result, Throwable throwable, MqttModel<String> model) {
        // 保存消费日志或记录消费结果
        if (throwable != null) {
            logger.error("消息消费失败，topic: {}", model.getTopic(), throwable);
        } else {
            logger.info("消息消费成功，topic: {}, result: {}", model.getTopic(), result);
        }
    }
}
```

**发送消息:**
```java
@Resource
private MqttUtils mqttUtils;

// 发送消息到指定主题
MqttModel<String> model = new MqttModel<>("default", "device/command", "{\"action\":\"restart\"}");
model.setQos(2);                     // 设置 QoS
model.setRetained(false);            // 是否保留消息
mqttUtils.send(model);

// 指定租户发送
MqttModel<String> model = new MqttModel<>("tenant_001", "device/command", "payload");
model.setQos(1);
mqttUtils.send(model);
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

---

### 7️⃣ 定时任务

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

### 8️⃣ Redis 缓存

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

### 9️⃣ MongoDB

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

### 1️⃣0️⃣ 病毒扫描 (ClamAV)

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

### 1️⃣1️⃣ 机器人通知 (solomon-bot-notice)

轻量级多渠道机器人通知组件，支持企业微信、钉钉、飞书三大办公机器人，全类型消息发送、动态@、签名验证、异步发送等功能。

**✅ 已测试功能**：
- ✅ 企业微信：全类型消息、@用户/所有人、签名验证、卡片按钮（100% 测试通过）
- ✅ 钉钉：全类型消息、@用户/手机号/所有人、签名验证、卡片按钮、Feed流（100% 测试通过）
- 🔄 飞书：功能已实现，待测试

**配置示例 (application.yml)**：
```yaml
solomon:
  notice:
    enabled: true
    global-signature: true
    signature: 【Solomon系统通知】
    
    # 企业微信机器人
    wechat-work:
      webhook-url: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=你的key
      secret: 你的签名密钥
    
    # 钉钉机器人
    ding-talk:
      webhook-url: https://oapi.dingtalk.com/robot/send?access_token=你的token
      secret: 你的签名密钥
        
    # 飞书机器人
    feishu:
      webhook-url: https://open.feishu.cn/open-apis/bot/v2/hook/你的hook_key
      secret: 你的签名密钥
```

**使用示例**：
```java
@Autowired
private NoticeUtils noticeUtils;

// 1. 一行代码发送通知（默认Markdown格式）
noticeUtils.send(
    NoticeChannelEnum.WECHAT_WORK,  // 渠道
    "通知标题",                     // 标题
    "这是通知内容"                  // 内容
);

// 2. 发送紧急告警并@所有人
noticeUtils.send(
    NoticeChannelEnum.DING_TALK,
    "紧急告警",
    "CPU使用率超过90%",
    true, // @所有人
    List.of("13800138000") // 额外@指定手机号用户
);

// 3. 发送交互卡片消息（带按钮）
NoticeMessage cardMsg = new NoticeMessage();
cardMsg.setMsgType(NoticeMsgTypeEnum.CARD);
cardMsg.setChannels(List.of(NoticeChannelEnum.WECHAT_WORK, NoticeChannelEnum.DING_TALK));
cardMsg.setLevel(NoticeLevelEnum.ERROR); // 消息等级：红色显示
cardMsg.setTitle("审批通知");
cardMsg.setContent("您有一个待审批的请假申请\n申请人：张三\n天数：3天");
cardMsg.setLinkPicUrl("https://example.com/leave.jpg"); // 企业微信有图自动用news_notice类型
// 添加按钮（最多支持3个）
cardMsg.setButtons(List.of(
    new NoticeMessage.Button("同意", "https://example.com/approve/123"),
    new NoticeMessage.Button("拒绝", "https://example.com/reject/123")
));
noticeUtils.send(cardMsg);

// 4. 多渠道同时发送（企业微信+钉钉同时收到）
NoticeMessage message = new NoticeMessage();
message.setChannels(List.of(
    NoticeChannelEnum.WECHAT_WORK, 
    NoticeChannelEnum.DING_TALK
));
message.setTitle("系统异常");
message.setContent("CPU使用率超过90%");
message.setLevel(NoticeLevelEnum.ERROR);
message.setAtAll(true); // 所有渠道都@所有人
message.setAsync(true); // 异步发送不阻塞业务
noticeUtils.send(message);
```

**支持的消息类型**：
| 消息类型 | 企业微信 | 钉钉 | 飞书 | 说明 |
|---------|---------|-----|-----|-----|
| TEXT（纯文本） | ✅ | ✅ | 🔄 | 普通文字消息，支持@ |
| MARKDOWN（富文本） | ✅ | ✅ | 🔄 | 富文本格式，支持@ |
| LINK（链接图文） | ✅ | ✅ | 🔄 | 带链接的消息 |
| IMAGE（图片） | ✅ | ✅ | 🔄 | 发送图片 |
| FILE（文件） | ✅ | ✅ | 🔄 | 发送任意文件 |
| VOICE（语音） | ✅ | ✅ | 🔄 | 发送语音消息 |
| CARD（交互卡片） | ✅ | ✅ | 🔄 | 带按钮的卡片，支持点击跳转 |
| FEED_CARD（Feed流卡片） | ❌ | ✅ | ❌ | 多图文消息，钉钉专属 |

---

### 1️⃣2️⃣ GS1 EPC 编解码 (solomon-epc-coder)

`solomon-epc-coder` 用于 GS1 条码和 EPC RFID 标签编码之间的生成、译码和反译，适用于一般 RFID 贴标单品、物流单元（纸箱、栈板）以及资产、车辆等场景。

**主要能力**：
- 支持 `AI 01 + AI 21`，即 GTIN + 序列号，并且 `AI 01`、`AI 21` 独立字段传入和输出。
- 支持 `SGTIN-96`、`SGTIN-198`。
- 支持 `AI 8004`，即 GIAI 资产标识。
- 支持 `GIAI-96`、`GIAI-202`。
- 支持 EPC 十六进制自动识别类型并反译。
- 使用 `BaseException` 统一报错，并支持 i18n 国际化错误信息。
- 支持链式调用。

**依赖引入**：
```xml
<dependency>
    <groupId>com.steven</groupId>
    <artifactId>solomon-epc-coder</artifactId>
    <version>1.0</version>
</dependency>
```

**SGTIN-96：AI 01 和 AI 21 分开传入**：
```java
EpcService epcService = new EpcService();

EpcResult encodeResult = epcService.gs1()
    .ai01("06901234567892")
    .ai21("1234567890")
    .companyPrefixLength(6)
    .tagSize(96)
    .encode();

EpcResult decodeResult = epcService.decodeEpc(encodeResult.getHex());

String ai01 = decodeResult.getAi01();
String ai21 = decodeResult.getAi21();
```

**SGTIN-198：支持字母数字序列号**：
```java
EpcResult encodeResult = epcService.gs1()
    .ai01("06901234567892")
    .ai21("ABC123")
    .companyPrefixLength(6)
    .tagSize(198)
    .encode();

EpcResult decodeResult = epcService.decodeEpc(encodeResult.getHex());
```

**GIAI-96：AI 8004 数字资产标识**：
```java
EpcResult encodeResult = epcService.gs1()
    .ai8004("690123123456")
    .companyPrefixLength(6)
    .tagSize(96)
    .encode();

EpcResult decodeResult = epcService.decodeEpc(encodeResult.getHex());

String ai8004 = decodeResult.getAi8004();
String assetReference = decodeResult.getAssetReference();
```

**GIAI-202：AI 8004 字母数字资产标识**：
```java
EpcResult encodeResult = epcService.gs1()
    .ai8004("690123ASSET001")
    .companyPrefixLength(6)
    .tagSize(202)
    .encode();

EpcResult decodeResult = epcService.decodeEpc(encodeResult.getHex());
```

**GS1 条码译码**：
```java
Gs1BarcodeResult gs1Result = epcService.parseGs1Barcode("(01)06901234567892(21)ABC123");

String ai01 = gs1Result.getAi01();
String ai21 = gs1Result.getAi21();
String ai8004 = gs1Result.getAi8004();
```

**main 方法测试**：
```java
com.steven.solomon.EpcCoderMainTest
```

该 main 方法覆盖以下类型的 GS1 译码、EPC 生成和 EPC 反译：
- `SGTIN-96`
- `SGTIN-198`
- `GIAI-96`
- `GIAI-202`

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

## 📚 接入与模块说明

### 模块职责

| 模块 | 主要职责 | 典型使用场景 | 关键依赖 |
|------|----------|--------------|----------|
| `solomon-constant` | 通用常量、错误码、上下文 Holder、基础 VO/Param | 所有业务服务的基础模型和错误码 | Spring Context、AOP、SLF4J |
| `solomon-utils` | JSON、日期、校验、加密、Spring、ClamAV 等工具 | 通用工具能力、自动 JSON 配置、病毒扫描 | Spring Boot AutoConfigure、Jackson、Hutool |
| `solomon-base` | Swagger、全局异常处理、基础自动配置 | Web/API 服务的基础能力 | Knife4j、Springdoc、Servlet API |
| `solomon-common` | WebConfig、请求过滤器、Controller 日志切面、Excel 工具 | Spring MVC 应用公共能力 | Spring Web、POI、FastExcel |
| `solomon-redis` | 多租户 Redis、缓存服务、Redis 队列消费 | 缓存、租户隔离、Redis 消息监听 | Spring Data Redis、Lettuce |
| `solomon-mongodb` | 多租户 MongoDB、动态 MongoTemplate、集合初始化 | 文档数据库、多租户数据隔离 | Spring Data MongoDB |
| `solomon-rabbitMq` | RabbitMQ 自动声明、发送工具、注解式监听 | Direct/Fanout/Topic/Headers/Delay 队列 | Spring AMQP |
| `solomon-mqtt` | Spring Integration MQTT 封装 | MQTT 3.x 消息发布订阅 | Spring Integration MQTT |
| `solomon-mqtt5` | Paho MQTT v5 封装 | MQTT 5 消息发布订阅 | Eclipse Paho MQTT v5 |
| `solomon-vertx-mqtt` | Vert.x MQTT 封装 | 响应式 MQTT 场景 | Vert.x MQTT |
| `solomon-mica-mqtt` | Mica MQTT 封装 | 高性能 MQTT 客户端 | mica-mqtt starter |
| `solomon-s3` | 统一文件服务、多云对象存储、缩略图、病毒扫描 | 文件上传下载、预签名分享、多云适配 | AWS SDK、MinIO、各云厂商 SDK |
| `solomon-xxlJob` | XXL-Job 执行器和任务自动创建 | 分布式任务调度 | xxl-job-core |
| `solomon-powerjob` | PowerJob worker 和任务自动创建 | 分布式任务调度 | powerjob worker starter |
| `solomon-gateway-sentinel` | Spring Cloud Gateway + Sentinel | 网关限流、熔断、规则加载 | Spring Cloud Gateway、Sentinel |
| `solomon-bot-notice` | 企业微信、钉钉、飞书机器人通知 | 告警、业务通知、异步通知 | Hutool HTTP、BouncyCastle |
| `solomon-epc-coder` | GS1 EPC 编解码 | SGTIN/GIAI 编码、解析、反译 | solomon-utils |

### 最小接入示例

只接入基础 Web 能力:

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-common</artifactId>
  <version>1.0</version>
</dependency>
```

接入对象存储:

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-s3</artifactId>
  <version>1.0</version>
</dependency>
```

接入 Redis 多租户缓存:

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-redis</artifactId>
  <version>1.0</version>
</dependency>
```

接入机器人通知:

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-bot-notice</artifactId>
  <version>1.0</version>
</dependency>
```

## ⚙️ 配置项速查

### 通用配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `i18n.language` | `zh` | 默认语言 |
| `i18n.all-locale` | `zh` | 需要加载的语言列表，多个值用逗号分隔 |
| `i18n.path` | 空 | 额外 i18n basename |
| `i18n.is-scan-class` | `true` | 是否扫描 classpath 下的 `i18n/messages` |

### 对象存储 `file`

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `file.choice` | 依代码默认值 | 存储类型，如 `MINIO`、`OSS`、`OBS`、`COS`、`S3` |
| `file.endpoint` | 空 | 对象存储服务地址 |
| `file.accessKey` | 空 | 访问密钥 |
| `file.secretKey` | 空 | 私密密钥 |
| `file.regionName` | 空 | 区域名称 |
| `file.bucket-name` | 空 | 默认 bucket |
| `file.root-directory` | 空 | 文件根目录前缀 |
| `file.file-naming-method` | 依代码默认值 | 文件命名方式，如 `ORIGINAL`、`DATE`、`UUID`、`SNOWFLAKE` |
| `file.part-size` | 依代码默认值 | 分片大小，单位 MB |
| `file.path-style-access-enabled` | 依代码默认值 | 是否启用 path-style 访问 |

### ClamAV `clamav`

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `clamav.enabled` | 依代码默认值 | 是否启用病毒扫描 |
| `clamav.host` | 依代码默认值 | ClamAV 服务地址 |
| `clamav.port` | 依代码默认值 | ClamAV 服务端口 |
| `clamav.platform` | 依代码默认值 | ClamAV 平台类型 |

### Redis

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.redis.enabled` | `true` | 是否启用 Redis 自动配置 |
| `spring.redis.mode` | 依代码默认值 | 租户模式 |
| `spring.redis.multiple` | 空 | 多租户 Redis 配置列表 |
| `spring.cache.mode` | 依代码默认值 | 缓存 key 处理模式 |

### MongoDB

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.data.mongodb.enabled` | `true` | 是否启用 MongoDB 自动配置 |
| `spring.data.mongodb.mode` | 依代码默认值 | 租户模式 |
| `spring.data.mongodb.multiple` | 空 | 多租户 MongoDB 配置列表 |

### MQTT

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `mqtt.enabled` | `true` | 是否启用 MQTT 自动配置 |
| `mqtt.mode` | 依代码默认值 | MQTT 租户模式 |
| `mqtt.multiple` | 空 | 多 broker 配置列表 |

### RabbitMQ

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.rabbitmq.enabled` | `true` | 是否启用 RabbitMQ 自动声明与工具 |
| `spring.rabbitmq.host` | Spring Boot 默认值 | RabbitMQ 地址 |
| `spring.rabbitmq.port` | Spring Boot 默认值 | RabbitMQ 端口 |
| `spring.rabbitmq.username` | Spring Boot 默认值 | 用户名 |
| `spring.rabbitmq.password` | Spring Boot 默认值 | 密码 |

### 任务调度

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `xxl.enabled` | 依代码默认值 | 是否启用 XXL-Job 任务创建 |
| `xxl.admin-addresses` | 空 | XXL-Job Admin 地址 |
| `xxl.access-token` | 空 | XXL-Job access token |
| `powerjob.worker.enabled` | 依代码默认值 | 是否启用 PowerJob worker |
| `powerjob.worker.server-address` | 空 | PowerJob 服务地址 |

### 机器人通知

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `solomon.notice.enabled` | `true` | 是否启用通知模块 |
| `solomon.notice.global-signature` | 依代码默认值 | 是否追加全局签名 |
| `solomon.notice.signature` | 空 | 全局签名内容 |
| `solomon.notice.wechat-work.webhook-url` | 空 | 企业微信机器人地址 |
| `solomon.notice.wechat-work.secret` | 空 | 企业微信签名密钥 |
| `solomon.notice.ding-talk.webhook-url` | 空 | 钉钉机器人地址 |
| `solomon.notice.ding-talk.secret` | 空 | 钉钉签名密钥 |
| `solomon.notice.feishu.webhook-url` | 空 | 飞书机器人地址 |
| `solomon.notice.feishu.secret` | 空 | 飞书签名密钥 |

## 🧩 版本兼容矩阵

| 项目版本 | JDK | Spring Boot | Spring Cloud | Spring Cloud Alibaba | 说明 |
|----------|-----|-------------|--------------|----------------------|------|
| `1.0` | 21 | 3.4.4 | 2024.0.0 | 2023.0.3.2 | 当前主线版本 |

构建和运行建议:

- 使用 JDK 21。
- 使用 Maven 3.9 或更高版本。
- Spring Boot 2.x 项目不建议直接接入当前版本。
- 如果业务项目仍在 Java 8/11/17，需要单独维护兼容分支。

## 🧯 常见问题排查

| 问题 | 常见原因 | 处理方式 |
|------|----------|----------|
| 启动时报 Bean 重复 | 业务项目和 starter 同时声明了同名 Bean | 优先使用业务自定义 Bean，starter 配置应配合 `@ConditionalOnMissingBean` |
| 自动配置没有生效 | 未引入对应模块，或 `enabled=false` | 检查依赖和配置开关，确认 `AutoConfiguration.imports` 被打入 jar |
| `ClamAvUtils` 缺失 | 未加载 `ClamAvConfig` 或依赖版本不完整 | 确认引入 `solomon-utils`，并检查 `clamav.enabled` 配置 |
| 文件上传失败 | endpoint、region、accessKey、secretKey 或 bucket 配置错误 | 先用对象存储控制台验证账号，再检查 `file.*` 配置 |
| Redis/Mongo 多租户切换失败 | tenantCode 未设置或租户连接未注册 | 检查请求头、`RequestHeaderHolder` 和 multiple 配置 |
| ThreadLocal 数据串租户 | 业务线程池中未清理上下文 | 使用 `try/finally` 或 `TenantContext.trySetFactory` |
| Controller 日志过大 | 请求/响应体较大或包含文件内容 | 生产环境建议限制日志长度并做敏感字段脱敏 |
| 编译失败提示 Java 版本不匹配 | 本机 JDK 低于 21 | 安装并切换到 JDK 21 |

## 🚢 发布与升级说明

### 发布前检查

1. 使用 JDK 21 执行完整构建。
2. 清理所有模块下的 `target/` 目录，确认没有构建产物进入 Git。
3. 执行单元测试和 starter 自动装配测试。
4. 检查 README、配置项和版本号是否同步。
5. 检查敏感信息，示例配置中不要提交真实 token、webhook、accessKey、secretKey。

### 版本号建议

建议使用语义化版本:

- 主版本号: 不兼容 API 或配置变更。
- 次版本号: 新增模块、新增功能、兼容性增强。
- 修订号: Bug 修复、文档修复、小范围优化。

示例:

```text
1.0.0  初始稳定版本
1.1.0  新增模块或能力
1.1.1  修复问题
2.0.0  破坏性升级
```

### 升级检查清单

| 检查项 | 说明 |
|--------|------|
| JDK 版本 | 当前版本要求 JDK 21 |
| Spring Boot 版本 | 当前版本基于 Spring Boot 3.4.4 |
| 自动配置入口 | Boot 3 优先使用 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |
| 配置项变更 | 升级前比对 `application.yml` 中的模块配置 |
| 外部服务版本 | Redis、MongoDB、RabbitMQ、MQTT broker、对象存储服务需要单独验证 |
| 敏感日志 | 升级后检查请求日志、任务日志、通知日志是否泄露密钥 |

### 模块接入原则

- 只引入实际使用的模块，避免把 MQ、S3、MongoDB 等重依赖全部带入业务服务。
- Web 服务优先引入 `solomon-common`；非 Web 服务优先只引入 `solomon-utils` 或具体中间件模块。
- 如果业务项目已经定义了 `ObjectMapper`、`RedisTemplate`、`RabbitTemplate` 等 Bean，starter 应让业务 Bean 优先生效。
- 生产环境不要使用 README 中的示例密码、默认 token 和本地地址。

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
