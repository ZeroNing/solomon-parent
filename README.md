# Solomon Parent

Solomon Parent 是一个基于 Java 21 和 Spring Boot 3 的企业基础设施组件库。它把常见的 Web 基础能力、异常处理、国际化、多租户上下文、缓存、消息队列、对象存储、任务调度、机器人通知和 GS1 EPC 编解码封装成独立模块，方便业务项目按需接入。

这个项目更适合作为内部基础组件库使用，而不是一个单体应用。业务项目应只引入自己需要的模块，避免把所有中间件能力一次性带入运行时。

## 快速信息

| 项目 | 说明 |
|------|------|
| GroupId | `com.steven` |
| Parent ArtifactId | `solomon-parent` |
| 当前版本 | `1.0` |
| JDK | 21 |
| Spring Boot | 3.4.4 |
| Spring Cloud | 2024.0.0 |
| Spring Cloud Alibaba | 2023.0.3.2 |
| 构建工具 | Maven |
| License | Apache License 2.0 |

## 模块总览

| 模块 | 职责 | 适合引入的场景 |
|------|------|----------------|
| `solomon-constant` | 常量、错误码、基础 VO/Param、上下文 Holder、租户上下文 | 所有业务项目的基础模型 |
| `solomon-utils` | JSON、日期、校验、加密、Spring 工具、ClamAV 工具 | 通用工具能力、JSON 自动配置、病毒扫描 |
| `solomon-base` | Swagger、基础自动配置、全局异常处理 | Web/API 项目 |
| `solomon-common` | WebConfig、请求过滤器、Controller 日志切面、Excel 工具 | Spring MVC 项目 |
| `solomon-redis` | 多租户 Redis、动态 RedisTemplate、缓存服务、Redis 队列监听 | 缓存、多租户缓存、Redis 消息 |
| `solomon-mongodb` | 多租户 MongoDB、动态 MongoTemplate、集合初始化 | MongoDB 文档库、多租户文档数据 |
| `solomon-rabbitMq` | RabbitMQ 发送工具、交换机/队列声明、注解式消费者 | RabbitMQ 消息队列 |
| `solomon-mqtt` | Spring Integration MQTT 封装 | MQTT 3.x |
| `solomon-mqtt5` | Paho MQTT v5 封装 | MQTT 5 |
| `solomon-vertx-mqtt` | Vert.x MQTT 封装 | 响应式 MQTT 客户端 |
| `solomon-mica-mqtt` | Mica MQTT 封装 | 高性能 MQTT 客户端 |
| `solomon-s3` | 统一对象存储接口、多云适配、缩略图、文件扫描 | 文件上传、下载、分享、多云对象存储 |
| `solomon-xxlJob` | XXL-Job 执行器和任务自动创建 | XXL-Job 调度 |
| `solomon-powerjob` | PowerJob worker 和任务自动创建 | PowerJob 调度 |
| `solomon-gateway-sentinel` | Gateway + Sentinel 限流熔断 | Spring Cloud Gateway 网关 |
| `solomon-bot-notice` | 企业微信、钉钉、飞书机器人通知 | 告警、业务通知、异步通知 |
| `solomon-epc-coder` | GS1 EPC 编码、解码、反译 | SGTIN/GIAI/EPC 业务 |

## 目录结构

```text
solomon-parent/
├── docker/                         # 常见中间件 Docker Compose 示例
├── solomon-base/                   # Web 基础能力、Swagger、异常处理
├── solomon-common/                 # MVC 通用能力、过滤器、切面、Excel
├── solomon-constant/               # 常量、错误码、上下文、基础模型
├── solomon-utils/                  # 通用工具、JSON、ClamAV
├── solomon-redis/                  # Redis 与多租户缓存
├── solomon-mongodb/                # MongoDB 与多租户文档库
├── solomon-rabbitMq/               # RabbitMQ 封装
├── solomon-mqtt/                   # MQTT 3.x 封装
├── solomon-mqtt5/                  # MQTT 5 封装
├── solomon-vertx-mqtt/             # Vert.x MQTT 封装
├── solomon-mica-mqtt/              # Mica MQTT 封装
├── solomon-s3/                     # 对象存储封装
├── solomon-xxlJob/                 # XXL-Job 封装
├── solomon-powerjob/               # PowerJob 封装
├── solomon-gateway-sentinel/       # Gateway + Sentinel
├── solomon-bot-notice/             # 机器人通知
├── solomon-epc-coder/              # GS1 EPC 编解码
└── test-*/                         # 各模块示例项目
```

## 环境要求

| 环境 | 要求 |
|------|------|
| JDK | 21 |
| Maven | 3.9+ 建议 |
| Spring Boot 业务项目 | 3.x |

当前版本面向 Spring Boot 3 和 Jakarta 命名空间。Spring Boot 2.x、Java 8/11/17 项目不建议直接接入当前版本。

## 构建

```bash
mvn clean install
```

只构建某个模块:

```bash
mvn -pl solomon-s3 -am clean install
```

启用示例模块:

```bash
mvn -Ptest-modules clean test
```

> 建议后续补充 Maven Wrapper，避免本机 Maven 版本差异影响构建。

## 最小接入

业务项目可以按需引入模块。

### Web 基础能力

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-common</artifactId>
  <version>1.0</version>
</dependency>
```

### Redis

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-redis</artifactId>
  <version>1.0</version>
</dependency>
```

### MongoDB

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-mongodb</artifactId>
  <version>1.0</version>
</dependency>
```

### RabbitMQ

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-rabbitMq</artifactId>
  <version>1.0</version>
</dependency>
```

### 对象存储

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-s3</artifactId>
  <version>1.0</version>
</dependency>
```

### 机器人通知

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-bot-notice</artifactId>
  <version>1.0</version>
</dependency>
```

### GS1 EPC 编解码

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-epc-coder</artifactId>
  <version>1.0</version>
</dependency>
```

## 自动配置约定

所有 starter 模块应遵循以下原则:

- 使用 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 作为 Spring Boot 3 自动配置入口。
- 兼容旧项目时，可以保留 `META-INF/spring.factories`。
- 自动配置类优先使用 `@AutoConfiguration` 或 `@Configuration`。
- 外部能力使用 `@ConditionalOnClass` 判断类路径。
- 功能开关使用 `@ConditionalOnProperty`。
- 可被业务覆盖的 Bean 使用 `@ConditionalOnMissingBean`。
- 避免在 starter 中使用跨模块的 `@ComponentScan("com.steven.solomon")`。

## 核心能力

### 多租户上下文

`solomon-constant` 提供 `TenantContext` 和 `RequestHeaderHolder`，用于在当前线程保存租户信息。Redis、MongoDB 等模块可以根据租户上下文选择不同连接工厂。

常见租户信息:

```java
RequestHeaderHolder.setTenantId("10001");
RequestHeaderHolder.setTenantCode("tenant_001");
RequestHeaderHolder.setTenantName("Demo Tenant");
```

使用 ThreadLocal 时必须在请求结束或任务结束后清理。Web 请求由 `RequestFilter` 负责清理；线程池、异步任务、定时任务需要业务代码自行保证清理。

### 国际化

默认扫描 classpath 下的 `i18n/messages`。

```yaml
i18n:
  language: zh
  all-locale: zh,en
  path:
  is-scan-class: true
```

异常码、枚举描述和业务提示都可以通过 i18n 资源文件维护。

### 全局异常处理

`solomon-base` 提供基础异常处理器，统一输出错误码、错误信息、服务标识和 requestId。业务项目可以通过自定义异常处理 Bean 覆盖默认行为。

### JSON 配置

`solomon-utils` 内置 Jackson 配置:

- `Long` 和 `BigInteger` 序列化为字符串，避免前端精度丢失。
- Java Time 类型统一格式化。
- 默认忽略未知属性。
- 禁用空 Bean 序列化失败。

### 对象存储

`solomon-s3` 通过 `FileServiceInterface` 提供统一文件服务。

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

常见操作:

```java
FileUpload upload = fileService.upload(file, "bucket-name");
InputStream stream = fileService.download("demo.txt", "bucket-name");
String url = fileService.share("demo.txt", "bucket-name", 3600);
fileService.deleteFile("demo.txt", "bucket-name");
```

### Redis

`solomon-redis` 支持 RedisTemplate、缓存服务和多租户 Redis 工厂。

```yaml
spring:
  redis:
    enabled: true
    host: localhost
    port: 6379
```

### MongoDB

`solomon-mongodb` 支持动态 MongoTemplate 和多租户 MongoDB 初始化。

```yaml
spring:
  data:
    mongodb:
      enabled: true
      host: localhost
      port: 27017
      database: demo
```

### RabbitMQ

`solomon-rabbitMq` 封装常见交换机、队列和消息发送能力。

```yaml
spring:
  rabbitmq:
    enabled: true
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### MQTT

项目提供多种 MQTT 实现:

| 模块 | 协议/实现 | 建议场景 |
|------|-----------|----------|
| `solomon-mqtt` | Spring Integration MQTT | 常规 MQTT 3.x |
| `solomon-mqtt5` | Eclipse Paho MQTT v5 | 需要 MQTT 5 特性 |
| `solomon-vertx-mqtt` | Vert.x | 响应式或高并发场景 |
| `solomon-mica-mqtt` | Mica MQTT | 高性能客户端场景 |

通用配置前缀:

```yaml
mqtt:
  enabled: true
```

### 任务调度

XXL-Job:

```yaml
xxl:
  enabled: true
  admin-addresses: http://localhost:8080/xxl-job-admin
  access-token: default_token
```

PowerJob:

```yaml
powerjob:
  worker:
    enabled: true
    server-address: localhost:7700
```

### 机器人通知

`solomon-bot-notice` 支持企业微信、钉钉、飞书机器人。

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

使用示例:

```java
noticeUtils.send(
    NoticeChannelEnum.DING_TALK,
    "任务告警",
    "任务执行失败",
    List.of()
);
```

### GS1 EPC 编解码

`solomon-epc-coder` 支持 SGTIN 和 GIAI 的 GS1 解析、EPC 编码和反译。

```java
EpcResult result = epcService.gs1()
    .ai01("06901234567892")
    .ai21("1234567890")
    .companyPrefixLength(6)
    .tagSize(96)
    .encode();
```

## 配置项速查

### 通用

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `i18n.language` | `zh` | 默认语言 |
| `i18n.all-locale` | `zh` | 加载的语言列表 |
| `i18n.path` | 空 | 额外 i18n basename |
| `i18n.is-scan-class` | `true` | 是否扫描 classpath 下的 i18n 资源 |

### 对象存储

| 配置项 | 说明 |
|--------|------|
| `file.choice` | 存储类型，如 `MINIO`、`OSS`、`OBS`、`COS`、`S3` |
| `file.endpoint` | 服务地址 |
| `file.accessKey` | 访问密钥 |
| `file.secretKey` | 私密密钥 |
| `file.regionName` | 区域 |
| `file.bucket-name` | 默认 bucket |
| `file.root-directory` | 根目录前缀 |
| `file.file-naming-method` | 文件命名方式 |
| `file.part-size` | 分片大小，单位 MB |

### 中间件开关

| 配置项 | 说明 |
|--------|------|
| `spring.redis.enabled` | 是否启用 Redis 配置 |
| `spring.data.mongodb.enabled` | 是否启用 MongoDB 配置 |
| `spring.rabbitmq.enabled` | 是否启用 RabbitMQ 配置 |
| `mqtt.enabled` | 是否启用 MQTT 配置 |
| `solomon.notice.enabled` | 是否启用机器人通知 |
| `clamav.enabled` | 是否启用病毒扫描 |

## Docker 示例

`docker/` 目录提供常见中间件的 Docker Compose 示例，包含 Redis、MongoDB、RabbitMQ、Nacos、Sentinel、XXL-Job、PowerJob、Kafka、Zookeeper、Nginx、Jenkins、Nexus、SonarQube 等。

示例:

```bash
cd docker/nacos
docker-compose up -d
```

## 版本兼容

| Solomon | JDK | Spring Boot | Spring Cloud | Spring Cloud Alibaba |
|---------|-----|-------------|--------------|----------------------|
| 1.0 | 21 | 3.4.4 | 2024.0.0 | 2023.0.3.2 |

## 常见问题

| 问题 | 常见原因 | 处理方式 |
|------|----------|----------|
| 自动配置没有生效 | 未引入模块、配置开关关闭、jar 中缺少自动配置文件 | 检查依赖、`enabled` 配置和 `AutoConfiguration.imports` |
| Bean 重复 | 业务项目和 starter 同时声明 Bean | 业务 Bean 优先，starter Bean 使用 `@ConditionalOnMissingBean` |
| `ClamAvUtils` 缺失 | ClamAV 自动配置未加载 | 确认引入 `solomon-utils`，并检查自动配置文件 |
| 文件上传失败 | endpoint、region、密钥或 bucket 配置错误 | 先用对象存储控制台验证账号，再检查 `file.*` |
| Redis/Mongo 租户切换失败 | tenantCode 未设置或租户工厂未注册 | 检查请求头、`RequestHeaderHolder` 和多租户配置 |
| ThreadLocal 串数据 | 异步任务或线程池未清理上下文 | 使用 `try/finally` 或 `TenantContext.trySetFactory` |
| 编译失败 | JDK 版本低于 21 | 切换到 JDK 21 |

## 安全与生产建议

- 不要在配置文件中提交真实密码、token、webhook、accessKey、secretKey。
- Controller 请求/响应日志需要脱敏和长度限制。
- 生产环境应关闭不必要的调试日志。
- ThreadLocal 上下文必须在请求结束、任务结束后清理。
- 对象存储、Redis、MongoDB、MQTT、RabbitMQ 客户端应注意资源关闭和连接池配置。
- 示例配置中的默认密码只用于本地开发。

## 发布前检查

1. 使用 JDK 21 执行完整构建。
2. 执行单元测试和 starter 自动装配测试。
3. 确认所有模块下的 `target/` 没有进入 Git。
4. 检查自动配置文件是否指向真实存在的类。
5. 检查 README、配置项、版本号是否同步。
6. 检查示例配置没有真实敏感信息。

## 版本号建议

建议使用语义化版本:

| 版本段 | 含义 |
|--------|------|
| 主版本 | 不兼容 API 或配置变更 |
| 次版本 | 新增模块或兼容性功能 |
| 修订号 | Bug 修复、文档修复、小优化 |

示例:

```text
1.0.0  初始稳定版本
1.1.0  新增模块或能力
1.1.1  修复问题
2.0.0  破坏性升级
```

## 开发建议

- 每个 starter 都补充 `ApplicationContextRunner` 测试。
- 自动配置类尽量显式声明 Bean，避免跨模块扫描。
- 多云对象存储 SDK 后续可以拆成可选依赖，减少业务项目体积。
- `solomon-utils` 应继续削减重依赖，保持工具模块轻量。
- 发布前建议接入 Maven Enforcer、Spotless、SpotBugs 和 CI。

## 许可证

[Apache License 2.0](LICENSE)

## 联系方式

- 作者: steven
- 邮箱: cao136623@163.com
