# Solomon Parent

Solomon Parent 是一个基于 **Java 21**、**Spring Boot 3** 的微服务基础设施组件库。项目将业务系统常用的基础能力拆成独立模块，统一管理依赖版本、自动配置、工具能力和中间件接入方式。

这个仓库不是单体应用，而是一组可按需引入的基础组件。业务项目应该只依赖自己需要的模块，避免把 Redis、MongoDB、MQTT、对象存储、任务调度等能力一次性全部带入运行时。

## 快速信息

| 项目 | 内容 |
| --- | --- |
| GroupId | `com.steven` |
| ArtifactId | `solomon-parent` |
| Version | `1.0` |
| JDK | `21` |
| Spring Boot | `3.4.4` |
| Spring Cloud | `2024.0.0` |
| Spring Cloud Alibaba | `2023.0.3.2` |
| 构建工具 | Maven |
| License | Apache License 2.0 |

## 模块清单

| 模块 | 职责 |
| --- | --- |
| `solomon-constant` | 常量、错误码、基础模型、请求上下文、租户上下文 |
| `solomon-utils` | JSON、日期、校验、加密、Spring 工具、ClamAV 工具 |
| `solomon-base` | 基础自动配置、Swagger/OpenAPI、全局异常处理 |
| `solomon-common` | Web MVC 通用配置、请求过滤器、日志切面、Excel 工具 |
| `solomon-datasource` | 多数据源、数据库连接池、数据源相关基础封装 |
| `solomon-redis` | RedisTemplate、缓存服务、多租户 Redis、Redis 队列监听 |
| `solomon-mongodb` | MongoDB 自动配置、多租户 MongoTemplate、集合初始化 |
| `solomon-rabbitMq` | RabbitMQ 发送、交换机队列声明、消费者封装 |
| `solomon-mqtt-module` | MQTT 聚合模块，统一管理 MQTT SDK 与多种客户端实现 |
| `solomon-s3` | MinIO、OSS、OBS、COS、BOS、S3 等对象存储统一封装 |
| `solomon-xxlJob` | XXL-Job 执行器与任务创建封装 |
| `solomon-powerjob` | PowerJob Worker 与任务创建封装 |
| `solomon-gateway-sentinel` | Spring Cloud Gateway + Sentinel 网关限流熔断 |
| `solomon-bot-notice` | 企业微信、钉钉、飞书机器人通知 |
| `solomon-epc-coder` | GS1 EPC 编码、解码、反译能力 |

### MQTT 子模块

`solomon-mqtt-module` 是 MQTT 聚合模块，内部包含：

| 子模块 | 职责 |
| --- | --- |
| `solomon-mqtt-sdk` | MQTT 通用模型、注解、监听器、租户初始化能力 |
| `solomon-mqtt` | Spring Integration MQTT 实现 |
| `solomon-mqtt5` | Eclipse Paho MQTT v5 实现 |
| `solomon-vertx-mqtt` | Vert.x MQTT 实现 |
| `solomon-mica-mqtt` | Mica MQTT 实现 |

## 目录结构

```text
solomon-parent/
├── docker/                    # 常用中间件 Docker Compose 示例
├── solomon-constant/          # 常量、错误码、上下文、基础模型
├── solomon-utils/             # 通用工具能力
├── solomon-base/              # Web 基础能力、OpenAPI、异常处理
├── solomon-common/            # MVC 通用能力、过滤器、切面
├── solomon-datasource/        # 数据源能力
├── solomon-redis/             # Redis 能力
├── solomon-mongodb/           # MongoDB 能力
├── solomon-rabbitMq/          # RabbitMQ 能力
├── solomon-mqtt-module/       # MQTT 聚合模块
├── solomon-s3/                # 对象存储能力
├── solomon-xxlJob/            # XXL-Job 能力
├── solomon-powerjob/          # PowerJob 能力
├── solomon-gateway-sentinel/  # 网关限流熔断能力
├── solomon-bot-notice/        # 机器人通知能力
├── solomon-epc-coder/         # GS1 EPC 编解码
└── test-*/                    # 示例与验证模块
```

## 环境要求

| 环境 | 要求 |
| --- | --- |
| JDK | 21 |
| Maven | 3.9+ 推荐 |
| Spring Boot 业务项目 | 3.x |

当前版本面向 Spring Boot 3 和 Jakarta 命名空间，不建议直接接入 Java 8/11/17 或 Spring Boot 2.x 项目。

## 构建

完整构建：

```bash
mvn clean install
```

构建单个模块及其依赖：

```bash
mvn -pl solomon-s3 -am clean install
```

启用测试示例模块：

```bash
mvn -Ptest-modules clean test
```

从失败模块继续构建：

```bash
mvn <原构建参数> -rf :模块artifactId
```

## 依赖接入

业务项目按需引入模块：

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-common</artifactId>
  <version>1.0</version>
</dependency>
```

常用模块示例：

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-redis</artifactId>
  <version>1.0</version>
</dependency>

<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-mongodb</artifactId>
  <version>1.0</version>
</dependency>

<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-s3</artifactId>
  <version>1.0</version>
</dependency>

<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-bot-notice</artifactId>
  <version>1.0</version>
</dependency>
```

MQTT 实现模块位于 `solomon-mqtt-module` 聚合模块下，引入时使用对应 artifact：

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-mqtt5</artifactId>
  <version>1.0</version>
</dependency>
```

## 自动配置约定

所有 starter 类模块应遵循以下约定：

- Spring Boot 3 使用 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。
- 需要兼容旧加载方式时，可保留 `META-INF/spring.factories`。
- 外部中间件能力使用 `@ConditionalOnClass`。
- 功能开关使用 `@ConditionalOnProperty`。
- 默认 Bean 使用 `@ConditionalOnMissingBean`，允许业务侧覆盖。
- 避免在 starter 中做大范围 `@ComponentScan`。
- 配置类使用清晰的 `@ConfigurationProperties` 前缀。

## 核心能力

### OpenAPI / Knife4j

项目使用 Springdoc OpenAPI 与 Knife4j：

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
knife4j:
  enable: true
```

常用访问地址：

| 页面 | 地址 |
| --- | --- |
| Swagger UI | `/swagger-ui.html` |
| Knife4j UI | `/doc.html` |
| OpenAPI JSON | `/v3/api-docs` |

### 多租户上下文

`solomon-constant` 提供租户与请求上下文能力，供 Redis、MongoDB、MQTT 等模块按租户路由。

```java
RequestHeaderHolder.setTenantId("10001");
RequestHeaderHolder.setTenantCode("tenant_001");
RequestHeaderHolder.setTenantName("Demo Tenant");
```

使用 ThreadLocal 场景必须在请求结束、任务结束或线程复用前清理上下文。

### 国际化

默认扫描 classpath 下的 `i18n/messages`：

```yaml
i18n:
  language: zh
  all-locale: zh,en
  is-scan-class: true
```

异常码、枚举描述、业务提示都可以通过 i18n 资源文件维护。

### JSON 配置

`solomon-utils` 内置 Jackson 约定：

- `Long`、`BigInteger` 序列化为字符串，避免前端精度丢失。
- Java Time 类型统一格式化。
- 默认忽略未知属性。
- 禁用空 Bean 序列化失败。

### 对象存储

`solomon-s3` 提供统一文件服务接口，屏蔽不同云厂商 SDK 差异。

```yaml
file:
  choice: MINIO
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucket-name: default-bucket
  file-naming-method: UUID
  part-size: 5
```

常见能力：

```java
FileUpload upload = fileService.upload(file, "bucket-name");
InputStream stream = fileService.download("demo.txt", "bucket-name");
String url = fileService.share("demo.txt", "bucket-name", 3600);
fileService.deleteFile("demo.txt", "bucket-name");
```

### Redis

```yaml
spring:
  redis:
    enabled: true
    host: localhost
    port: 6379
```

支持普通 RedisTemplate、缓存服务、多租户 Redis 工厂和 Redis 队列监听。

### MongoDB

```yaml
spring:
  data:
    mongodb:
      enabled: true
      host: localhost
      port: 27017
      database: demo
```

支持动态 MongoTemplate、多租户 MongoDB 和集合初始化。

### RabbitMQ

```yaml
spring:
  rabbitmq:
    enabled: true
    host: localhost
    port: 5672
    username: guest
    password: guest
```

支持交换机、队列、绑定关系声明，以及统一消息发送入口。

### MQTT

```yaml
mqtt:
  enabled: true
```

建议优先复用 `solomon-mqtt-sdk` 中的通用模型、监听器和租户初始化能力，再按业务场景选择具体 MQTT 实现。

### 机器人通知

`solomon-bot-notice` 支持企业微信、钉钉、飞书机器人。不同平台的消息类型能力不完全一致，模块会按平台能力构造原生报文，无法原生支持的类型应降级为 Markdown 或卡片展示。

```yaml
solomon:
  notice:
    enabled: true
    global-signature: true
    signature: Solomon
    wechat-work:
      webhook-url:
      secret:
    ding-talk:
      webhook-url:
      secret:
    feishu:
      webhook-url:
      secret:
```

链式调用示例：

```java
noticeUtils.send(
    NoticeMessage.of(NoticeChannelEnum.DING_TALK, "任务告警", "任务执行失败")
        .level(NoticeLevelEnum.ERROR)
        .at(false, List.of("13800000000"))
        .async(true)
);
```

图片、文件、语音等媒体消息需要先在对应平台获取媒体资源标识，再传入消息模型。

### 任务调度

XXL-Job：

```yaml
xxl:
  enabled: true
  admin-addresses: http://localhost:8080/xxl-job-admin
  access-token: default_token
```

PowerJob：

```yaml
powerjob:
  worker:
    enabled: true
    server-address: localhost:7700
```

### GS1 EPC 编解码

```java
EpcResult result = epcService.gs1()
    .ai01("06901234567892")
    .ai21("1234567890")
    .companyPrefixLength(6)
    .tagSize(96)
    .encode();
```

## Docker 示例

`docker/` 目录提供常见中间件示例，例如 Redis、MongoDB、RabbitMQ、Nacos、Sentinel、XXL-Job、PowerJob、Kafka、Zookeeper、Nginx、Jenkins、Nexus、SonarQube 等。

```bash
cd docker/nacos
docker-compose up -d
```

## 配置速查

| 配置项 | 说明 |
| --- | --- |
| `i18n.language` | 默认语言 |
| `i18n.all-locale` | 加载语言列表 |
| `i18n.is-scan-class` | 是否扫描 classpath 下的 i18n 资源 |
| `spring.redis.enabled` | 是否启用 Redis |
| `spring.data.mongodb.enabled` | 是否启用 MongoDB |
| `spring.rabbitmq.enabled` | 是否启用 RabbitMQ |
| `mqtt.enabled` | 是否启用 MQTT |
| `solomon.notice.enabled` | 是否启用机器人通知 |
| `clamav.enabled` | 是否启用 ClamAV 文件扫描 |
| `file.choice` | 对象存储类型 |
| `file.endpoint` | 对象存储服务地址 |
| `file.bucket-name` | 默认 Bucket |

## 开发规范

- 新模块必须有明确边界，不把业务逻辑写进基础组件。
- 公共模型、注解、接口优先放到 SDK 或基础模块。
- 自动配置必须允许业务项目覆盖默认 Bean。
- 配置项命名应稳定、清晰，避免随意缩写。
- 代码注释使用中文，解释设计意图和复杂逻辑，不注释显而易见的赋值。
- 不提交真实密码、token、Webhook、AccessKey、SecretKey。
- 不提交 `target/`、本地 IDE 缓存、临时文件。
- 提交前使用 JDK 21 执行完整构建。

## 发布前检查

1. 使用 JDK 21 执行 `mvn clean install`。
2. 必要时执行 `mvn -Ptest-modules clean test`。
3. 检查所有自动配置类是否在 `AutoConfiguration.imports` 中声明。
4. 检查 README、配置项、版本号与代码一致。
5. 检查示例配置不包含真实敏感信息。
6. 检查新增模块是否已加入父 POM 的 `modules` 和 `dependencyManagement`。

## 常见问题

| 问题 | 常见原因 | 处理方式 |
| --- | --- | --- |
| 编译失败 | JDK 版本低于 21 | 切换到 JDK 21 |
| 自动配置未生效 | 未引入模块、开关关闭、自动配置文件缺失 | 检查依赖、配置和 `AutoConfiguration.imports` |
| Bean 重复 | 业务项目和 starter 同时声明 Bean | starter 使用 `@ConditionalOnMissingBean`，业务 Bean 优先 |
| 中间件连接失败 | endpoint、账号、密码、网络不正确 | 先用中间件控制台或客户端验证连接 |
| 多租户路由失败 | 租户上下文未设置或未清理 | 检查请求头、上下文 Holder 和线程池清理逻辑 |
| 机器人消息发送失败 | Webhook、secret、媒体资源 ID 不正确 | 检查平台机器人配置和平台返回结果 |

## License

[Apache License 2.0](LICENSE)

## Author

- Author: steven
- Email: cao136623@163.com
