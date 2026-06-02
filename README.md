<div align="center">

# Solomon Parent

**面向 Spring Boot 微服务的基础设施组件库**

统一沉淀缓存、MQTT、对象存储、任务调度、机器人通知、网关限流、通用工具等工程能力，让业务项目按需引入、少写重复代码、少踩集成坑。

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-green)
![Maven](https://img.shields.io/badge/Build-Maven-orange)
![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey)

</div>

## 项目简介

Solomon Parent 是一个基础设施型 Maven 多模块工程，目标是把微服务项目里反复出现的技术集成沉淀成可复用组件。

项目遵循“公共 SDK + 独立实现模块”的拆分方式。例如缓存注解放在 `solomon-cache-sdk`，Redis 实现放在 `solomon-cache-redis`；对象存储公共接口放在 `solomon-s3-sdk`，MinIO、OSS、OBS、COS、BOS、Amazon S3 分别独立实现。业务系统只需要引入自己需要的模块，不需要被无关 SDK 污染依赖树。

## 核心特点

| 特性 | 说明 |
| --- | --- |
| 按能力拆分 | Redis、MQTT、S3、Job、Cache 等能力独立聚合，按需引入 |
| SDK 与实现分离 | 公共接口、注解、模型放入 SDK，具体实现独立维护 |
| 多租户友好 | 缓存、Redis MQTT、MongoDB、数据源等模块支持租户隔离场景 |
| 自动配置收敛 | 尽量通过 Spring Boot AutoConfiguration 减少业务侧样板代码 |
| 统一命名规则 | 缓存使用 `cache.*`，对象存储使用 `file.*`，减少配置歧义 |
| 可扩展 | 后续接入新的缓存介质、S3 供应商、MQTT 实现时不影响业务注解 |
| 测试模块隔离 | 测试工程统一移动到 `solomon-test-module`，默认构建不加载重型中间件测试 |

## 技术栈

| 类型 | 版本 |
| --- | --- |
| JDK | `21` |
| Maven | `3.x` |
| Spring Boot | `3.4.4` |
| Spring Cloud | `2024.0.0` |
| Spring Cloud Alibaba | `2023.0.3.2` |
| MyBatis Plus | `3.5.1` |
| PowerJob | `5.1.1` |
| XXL-JOB | `3.0.0` |
| MinIO | `8.5.17` |
| AWS S3 SDK | `2.39.4` |

## 模块总览

| 模块 | 类型 | 职责 |
| --- | --- | --- |
| `solomon-constant` | 基础模块 | 常量、错误码、基础模型、请求上下文、租户上下文 |
| `solomon-utils` | 基础模块 | JSON、日期、校验、加密、Spring、ClamAV 等工具 |
| `solomon-base` | 基础模块 | 基础自动配置、OpenAPI、统一异常处理 |
| `solomon-common` | 基础模块 | Web MVC、过滤器、日志切面、Excel 等通用能力 |
| `solomon-persistence-module` | 持久化模块 | SDK、主流数据库方言、深浅分页、多租户数据源、HikariCP/Druid、报表查询 |
| `solomon-mongodb` | 数据模块 | MongoDB 自动配置、多租户 MongoTemplate、集合初始化 |
| `solomon-redis` | 历史模块 | 历史 Redis 能力，保留给旧业务使用 |
| `solomon-cache-module` | 聚合模块 | 通用缓存 SDK、Redis 缓存实现、Caffeine 本地缓存实现 |
| `solomon-rabbitMq` | 消息模块 | RabbitMQ 发送、交换机队列声明、消费者封装 |
| `solomon-mqtt-module` | 聚合模块 | 多种 MQTT Client 实现、Redis MQTT、多租户初始化 |
| `solomon-s3-module` | 聚合模块 | 对象存储公共 SDK 与多供应商实现 |
| `solomon-job-module` | 聚合模块 | PowerJob、XXL-JOB 统一任务注解与自动注册 |
| `solomon-gateway-security` | 网关模块 | Spring Cloud Gateway + Spring Security 多租户 JWT 鉴权 |
| `solomon-gateway-sentinel` | 网关模块 | Spring Cloud Gateway + Sentinel 限流熔断 |
| `solomon-bot-notice` | 通知模块 | 企业微信、钉钉、飞书机器人通知 |
| `solomon-epc-coder` | 工具模块 | GS1 EPC 编码、解码、反译 |
| `solomon-test-module` | 测试模块 | 测试用例聚合模块，仅在 `test-modules` profile 中启用 |

## 工程结构

```text
solomon-parent
├── solomon-constant
├── solomon-utils
├── solomon-base
├── solomon-common
├── solomon-persistence-module
├── solomon-mongodb
├── solomon-redis
├── solomon-cache-module
│   ├── solomon-cache-sdk
│   ├── solomon-cache-redis
│   └── solomon-cache-caffeine
├── solomon-mqtt-module
│   ├── solomon-mqtt-sdk
│   ├── solomon-mqtt
│   ├── solomon-mqtt5
│   ├── solomon-vertx-mqtt
│   ├── solomon-mica-mqtt
│   └── solomon-redis-mqtt
├── solomon-s3-module
│   ├── solomon-s3-sdk
│   ├── solomon-minio
│   ├── solomon-oss
│   ├── solomon-obs
│   ├── solomon-cos
│   ├── solomon-bos
│   └── solomon-amazon-s3
├── solomon-job-module
│   ├── solomon-job-sdk
│   ├── solomon-powerjob
│   └── solomon-xxlJob
└── solomon-test-module
    └── test-*
```

## 快速开始

### 1. 引入父工程版本管理

业务项目可以通过父 POM 或 dependencyManagement 继承 Solomon 的版本管理。推荐在公司内部统一发布后，由业务项目只声明模块 artifactId。

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.steven</groupId>
      <artifactId>solomon-parent</artifactId>
      <version>1.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

### 2. 按能力引入模块

例如业务只需要 Redis 缓存：

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-cache-sdk</artifactId>
</dependency>
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-cache-redis</artifactId>
</dependency>
```

例如业务只需要 MinIO：

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-s3-sdk</artifactId>
</dependency>
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-minio</artifactId>
</dependency>
```

### 3. 构建项目

```bash
# 构建默认模块
mvn clean install

# 构建指定模块并自动构建依赖
mvn -pl solomon-cache-module -am clean test

# 构建 Redis MQTT 实现及其依赖
mvn -pl solomon-mqtt-module/solomon-redis-mqtt -am clean test

# 启用测试聚合模块
mvn -Ptest-modules clean test
```

## Cache 缓存模块

`solomon-cache-module` 是新的缓存统一入口。缓存注解、Key 规则、租户上下文、重复请求限制都放在 `solomon-cache-sdk`，具体缓存介质由实现模块提供。

### 子模块说明

| 子模块 | 职责 | 默认状态 |
| --- | --- | --- |
| `solomon-cache-sdk` | 通用缓存接口、注解、AOP、Key 构建、租户上下文 | 必选 |
| `solomon-cache-redis` | Redis 单机和多租户实现，关闭 Spring Boot Redis 默认注册 | 按配置启用 |
| `solomon-cache-caffeine` | 本地 Caffeine 缓存实现 | 默认关闭 |

### Key 模式

| 模式 | 行为 | 适用场景 |
| --- | --- | --- |
| `NONE` | 不处理缓存 Key | 完全由业务自己管理 Key |
| `PREFIX` | 增加固定业务前缀 | 单应用隔离缓存 |
| `TENANT_PREFIX` | 增加租户前缀 | 同一个 Redis 实例中隔离租户数据 |
| `TENANT_SWITCH` | 根据租户切换缓存实例 | 多租户独立 Redis 实例 |

### 通用注解

| 注解 | 说明 |
| --- | --- |
| `@CacheResult` | 方法结果缓存，支持自定义 Key、过期时间、Key 模式 |
| `@CacheRemove` | 删除缓存，支持精确 Key 和 Pattern 删除 |
| `@RepeatRequestLimit` | 重复请求限制，默认使用当前 URL + Token 作为唯一标识 |

### Redis 配置示例

```yaml
cache:
  redis:
    enabled: true
    host: 127.0.0.1
    port: 6379
    database: 0
    password:
    timeout: 3000
    key-prefix: app
```

### Caffeine 配置示例

```yaml
cache:
  caffeine:
    enabled: true
    spec: maximumSize=10000,expireAfterWrite=10m
```

### 注解使用示例

```java
@CacheResult(
    key = "'user:' + #userId",
    expireSeconds = 300,
    keyMode = CacheKeyMode.TENANT_PREFIX
)
public UserInfo getUserInfo(Long userId) {
    return userRepository.getById(userId);
}

@CacheRemove(
    key = "'user:' + #userId",
    keyMode = CacheKeyMode.TENANT_PREFIX
)
public void removeUserCache(Long userId) {
}
```

## MQTT 模块

`solomon-mqtt-module` 负责统一 MQTT 相关能力。公共模型、监听注解、租户初始化能力放入 `solomon-mqtt-sdk`，不同客户端实现独立成模块。

### 子模块说明

| 子模块 | 实现方式 | 说明 |
| --- | --- | --- |
| `solomon-mqtt-sdk` | 公共 SDK | MQTT 通用模型、注解、监听器、租户初始化能力 |
| `solomon-mqtt` | Spring Integration MQTT | 适合传统 Spring Integration 场景 |
| `solomon-mqtt5` | Eclipse Paho MQTT v5 | 支持 MQTT v5 协议 |
| `solomon-vertx-mqtt` | Vert.x MQTT | 适合异步、高并发场景 |
| `solomon-mica-mqtt` | Mica MQTT | 轻量化 MQTT Client 集成 |
| `solomon-redis-mqtt` | Redis Pub/Sub | 使用 Redis Pub/Sub 模拟 MQTT 风格消息能力 |

### SSL 支持

| 实现 | SSL 说明 |
| --- | --- |
| `solomon-mqtt` | 支持 `ssl://host:8883` |
| `solomon-mqtt5` | 支持 `ssl://host:8883` |
| `solomon-vertx-mqtt` | 支持 SSL 端口和证书配置 |
| `solomon-mica-mqtt` | 支持 Mica 自身 SSL 配置 |
| `solomon-redis-mqtt` | 通过 `cache.redis` 多租户配置启用 Redis SSL |

### Redis MQTT 接入示例

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-mqtt-sdk</artifactId>
</dependency>
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-redis-mqtt</artifactId>
</dependency>
```

```yaml
cache:
  redis:
    enabled: true
    host: 127.0.0.1
    port: 6379
    key-prefix: mqtt
```

### 异步发送

MQTT 模块提供统一异步发送能力。`solomon-mqtt` 和 `solomon-mqtt5` 已切换为 Paho `MqttAsyncClient`，`send` 只提交发送请求，不等待 Broker ACK；业务需要感知发送结果时调用 `sendAsync`。

```java
mqttOperations.sendAsync(message)
    .whenComplete((unused, throwable) -> {
        if (throwable != null) {
            // 中文注释：这里可以记录失败日志、告警或触发重试。
        }
    });
```

Vert.x MQTT 会绑定 `publish(...).onComplete(...)` 回调；Mica MQTT 和 Redis MQTT 没有统一的底层发送回调时，会通过统一接口降级为异步任务。

## S3 对象存储模块

`solomon-s3-module` 按供应商拆分对象存储实现。公共接口、配置模型、上传模型、命名规则、图片工具放在 `solomon-s3-sdk`，供应商 SDK 只存在于具体实现模块中。

### 子模块说明

| 子模块 | 供应商 | 说明 |
| --- | --- | --- |
| `solomon-s3-sdk` | 通用 SDK | 公共接口、配置、上传模型、文件命名、图片处理 |
| `solomon-minio` | MinIO | 私有化对象存储 |
| `solomon-oss` | 阿里云 OSS | 阿里云对象存储 |
| `solomon-obs` | 华为云 OBS | 华为云对象存储 |
| `solomon-cos` | 腾讯云 COS | 腾讯云对象存储 |
| `solomon-bos` | 百度云 BOS | 百度云对象存储 |
| `solomon-amazon-s3` | Amazon S3 | AWS S3 以及兼容 S3 协议的云厂商 |

### 供应商选择

```properties
file.choice=MINIO
```

可选值：

| 值 | 模块 |
| --- | --- |
| `MINIO` | `solomon-minio` |
| `OSS` | `solomon-oss` |
| `OBS` | `solomon-obs` |
| `COS` | `solomon-cos` |
| `BOS` | `solomon-bos` |
| `AMAZON_S3` | `solomon-amazon-s3` |

### MinIO 接入示例

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-s3-sdk</artifactId>
</dependency>
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-minio</artifactId>
</dependency>
```

```yaml
file:
  choice: MINIO
  endpoint: http://127.0.0.1:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: demo
```

## Job 任务调度模块

`solomon-job-module` 把 PowerJob 和 XXL-JOB 的业务入口统一为 `@JobTask`。业务侧只关注任务声明，自动注册、登录、保存、更新、启停由实现模块适配。

### 子模块说明

| 子模块 | 职责 |
| --- | --- |
| `solomon-job-sdk` | 统一 `JobTask` 注解、平台枚举、公共模型 |
| `solomon-powerjob` | PowerJob Worker、自动注册、任务启停、管理端接口适配 |
| `solomon-xxlJob` | XXL-JOB 执行器、自动注册、任务启停、Handler 注册 |

### 注解示例

```java
@JobTask(
    platforms = {JobPlatform.POWERJOB, JobPlatform.XXL_JOB},
    taskName = "库存同步",
    powerJob = @PowerJobTask(
        timeExpressionType = TimeExpressionType.CRON,
        timeExpression = "0 0/5 * * * ?"
    ),
    xxlJob = @XxlJobTask(
        scheduleType = ScheduleTypeEnum.CRON,
        scheduleConf = "0 0/5 * * * ?"
    )
)
public class StockSyncJob {
}
```

### 设计原则

| 原则 | 说明 |
| --- | --- |
| 多平台声明 | 一个任务可以注册到 PowerJob、XXL-JOB 或同时注册到两者 |
| 版本兼容 | 自动注册逻辑适配历史版本和新版本管理端接口 |
| 复用官方能力 | 优先复用 jar 包内已有接口和模型，减少硬编码 HTTP 参数 |
| 幂等注册 | 已存在任务走更新逻辑，避免重复创建 |

## Bot Notice 机器人通知

`solomon-bot-notice` 提供企业微信、钉钉、飞书机器人通知能力，支持文本、Markdown、图片、文件等多种消息类型。

| 平台 | 能力 |
| --- | --- |
| 企业微信 | 文本、Markdown、图片、文件等 |
| 钉钉 | 文本、Markdown、ActionCard、图片、文件等 |
| 飞书 | 文本、富文本、图片、文件等 |

## Test 测试模块

测试模块已统一移动到 `solomon-test-module`，根工程默认不会加载，避免普通构建时拉起大量中间件测试依赖。

```bash
# 执行全部测试模块
mvn -Ptest-modules clean test

# 只执行某个测试模块
mvn -Ptest-modules -pl solomon-test-module/test-solomon-s3 -am test
```

## 模块选择建议

| 需求 | 推荐引入 |
| --- | --- |
| Redis 缓存 | `solomon-cache-sdk` + `solomon-cache-redis` |
| 本地缓存 | `solomon-cache-sdk` + `solomon-cache-caffeine` |
| Redis MQTT | `solomon-mqtt-sdk` + `solomon-redis-mqtt` |
| Paho MQTT v3 | `solomon-mqtt-sdk` + `solomon-mqtt` |
| Paho MQTT v5 | `solomon-mqtt-sdk` + `solomon-mqtt5` |
| MinIO | `solomon-s3-sdk` + `solomon-minio` |
| 阿里云 OSS | `solomon-s3-sdk` + `solomon-oss` |
| Amazon S3 兼容存储 | `solomon-s3-sdk` + `solomon-amazon-s3` |
| PowerJob | `solomon-job-sdk` + `solomon-powerjob` |
| XXL-JOB | `solomon-job-sdk` + `solomon-xxlJob` |

## 开发规范

1. 公共 API、注解、模型、枚举优先放入 `*-sdk`。
2. 具体实现必须放入独立实现模块，避免 SDK 引入重型第三方依赖。
3. 新增模块必须同步维护根 `pom.xml` 的 `<modules>` 和 `<dependencyManagement>`。
4. 自动配置类只暴露必要 Bean，避免引入模块后和业务项目 Bean 冲突。
5. 配置前缀按能力命名，缓存统一使用 `cache.*`，对象存储统一使用 `file.*`。
6. Java 源码统一使用 UTF-8 无 BOM，避免 Maven 编译出现非法字符。
7. 注释使用中文说明关键业务意图，普通 getter/setter 和显而易见逻辑不写噪音注释。
8. 新增能力优先补充 README 和测试模块，保证后来的人能快速接手。

## 常见问题

### 为什么要拆成 `*-sdk` 和实现模块？

这样可以让业务只依赖稳定抽象，不被具体第三方 SDK 绑死。例如业务只写缓存注解，未来从 Redis 切到 Caffeine 或其他缓存时，业务代码不需要跟着改。

### 为什么 `solomon-redis` 还保留？

`solomon-redis` 是历史 Redis 能力，保留给旧业务平滑迁移。新缓存能力建议优先使用 `solomon-cache-module`。

### Redis MQTT 为什么依赖 `solomon-cache-redis`？

Redis 连接、租户切换、Key 前缀这些能力已经在缓存模块沉淀。Redis MQTT 复用同一套 Redis 基础设施，可以避免重复维护两套多租户 Redis 逻辑。

### S3 供应商模块怎么选？

业务项目引入 `solomon-s3-sdk` 和一个供应商模块，再通过 `file.choice` 指定供应商。例如 `MINIO`、`OSS`、`OBS`、`COS`、`BOS`、`AMAZON_S3`。

### 为什么测试模块默认不参与构建？

测试模块依赖的中间件较多，例如 Redis、MQTT Broker、S3 服务、任务调度平台。默认构建只验证基础组件，需要完整验证时再显式启用 `-Ptest-modules`。

## 版本规划

| 阶段 | 目标 |
| --- | --- |
| 当前版本 | 完成 Cache、MQTT、S3、Job 聚合模块拆分和基础能力收敛 |
| 下一阶段 | 补齐更多集成测试，完善各模块 README 和使用示例 |
| 长期方向 | 形成稳定的企业内部基础设施组件库，持续减少业务项目重复代码 |

## 许可证

本项目采用 Apache License 2.0 协议。
