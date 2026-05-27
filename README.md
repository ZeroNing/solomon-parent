# Solomon Parent

Solomon Parent 是基于 **Java 21**、**Spring Boot 3.4.4** 的基础设施组件库。项目按能力拆分模块，业务系统只引入自己需要的组件，避免把对象存储、MQTT、任务调度等第三方 SDK 一次性带入运行时。

## 快速信息

| 项目 | 内容 |
| --- | --- |
| GroupId | `com.steven` |
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
| `solomon-base` | 基础自动配置、OpenAPI、全局异常处理 |
| `solomon-common` | Web MVC 通用配置、过滤器、日志切面、Excel 工具 |
| `solomon-datasource` | 多数据源、连接池、数据源基础封装 |
| `solomon-redis` | RedisTemplate、缓存服务、多租户 Redis、Redis 队列监听 |
| `solomon-mongodb` | MongoDB 自动配置、多租户 MongoTemplate、集合初始化 |
| `solomon-rabbitMq` | RabbitMQ 发送、交换机队列声明、消费者封装 |
| `solomon-mqtt-module` | MQTT 聚合模块 |
| `solomon-s3-module` | 对象存储聚合模块 |
| `solomon-job-module` | 任务调度聚合模块 |
| `solomon-cache-module` | 缓存聚合模块，提供基础缓存 SDK 与 Redis 实现 |
| `solomon-gateway-sentinel` | Spring Cloud Gateway + Sentinel 限流熔断 |
| `solomon-bot-notice` | 企业微信、钉钉、飞书机器人通知 |
| `solomon-epc-coder` | GS1 EPC 编码、解码、反译 |

## 聚合模块

### MQTT

| 子模块 | 职责 |
| --- | --- |
| `solomon-mqtt-sdk` | MQTT 通用模型、注解、监听器、租户初始化能力 |
| `solomon-mqtt` | Spring Integration MQTT 实现 |
| `solomon-mqtt5` | Eclipse Paho MQTT v5 实现 |
| `solomon-vertx-mqtt` | Vert.x MQTT 实现 |
| `solomon-mica-mqtt` | Mica MQTT 实现 |
| `solomon-redis-mqtt` | Redis Pub/Sub MQTT 风格实现 |

MQTT 公共能力已收敛到 `solomon-mqtt-sdk`：客户端注册表使用并发 Map，监听器扫描统一处理 `enabled`、租户范围和 topic 表达式，消费者结束后会清理租户上下文。业务侧只引入一个实现模块，避免多个 MQTT 客户端自动配置互相覆盖。

四个 MQTT Broker 实现均支持 SSL：Paho MQTT3/5 和 Vert.x 使用 `ssl://host:8883`，Mica 会识别自身 `ssl.enabled` 配置或 8883 端口。`solomon-redis-mqtt` 支持多租户 Redis 配置，底层连接创建复用 `solomon-cache-redis`，Redis SSL 通过租户配置 `ssl=true` 开启。

### S3 / 对象存储

| 子模块 | 职责 |
| --- | --- |
| `solomon-s3-sdk` | 公共接口、配置属性、上传模型、命名规则、抽象文件服务、图片工具 |
| `solomon-minio` | MinIO 实现 |
| `solomon-oss` | 阿里云 OSS 实现 |
| `solomon-obs` | 华为云 OBS 实现 |
| `solomon-cos` | 腾讯云 COS 实现 |
| `solomon-bos` | 百度云 BOS 实现 |
| `solomon-amazon-s3` | Amazon S3 及 S3 协议兼容实现，如 R2、TOS、KODO 等 |

业务项目通常引入 `solomon-s3-sdk` 加一个供应商模块，通过 `file.choice` 选择供应商。

### Job / 任务调度

| 子模块 | 职责 |
| --- | --- |
| `solomon-job-sdk` | 统一 `JobTask` 注解、平台枚举、PowerJob/XXL-JOB 公共枚举 |
| `solomon-powerjob` | PowerJob Worker、自动注册、任务启停、管理端接口适配 |
| `solomon-xxlJob` | XXL-JOB 执行器、自动注册、任务启停、Handler 注册 |

`JobTask` 同时支持 PowerJob 与 XXL-JOB 字段。默认按任务类型自动识别平台，也可以通过 `platforms` 精确指定平台或同时注册到多个平台。

```java
@JobTask(
    platforms = {JobPlatform.POWERJOB},
    taskName = "库存同步",
    timeExpressionType = TimeExpressionType.CRON,
    timeExpression = "0 0/5 * * * ?"
)
public class StockSyncProcessor implements BasicProcessor {
    // 中文注释：业务代码只关注任务处理，注册参数由注解统一维护。
}
```

```java
@JobTask(
    platforms = {JobPlatform.XXL_JOB},
    taskName = "订单超时关闭",
    scheduleType = ScheduleTypeEnum.CRON,
    scheduleConf = "0 0/1 * * * ?",
    executorHandler = "orderTimeoutCloseJob",
    start = true
)
public class OrderTimeoutCloseHandler extends AbstractJobConsumer {
    @Override
    public void handle(String jobParam) {
        // 中文注释：XXL-JOB 任务参数由调度中心传入。
    }
}
```

### Cache / 缓存

| 子模块 | 职责 |
| --- | --- |
| `solomon-cache-sdk` | 缓存接口、多租户切换上下文、缓存注解、重复请求限制注解、缓存 key 构建工具 |
| `solomon-cache-redis` | 基于 `cache.redis` 的 Redis 单机/多租户注册、RedisTemplate、CacheService 自动装配 |
| `solomon-cache-caffeine` | 基于 `cache.caffeine` 的本地缓存实现，复用 SDK 通用缓存注解 |

缓存模块支持 `NONE`、`PREFIX`、`TENANT_PREFIX`、`TENANT_SWITCH` 四种 key 模式。`solomon-cache-sdk` 提供通用注解和 AOP，Redis、Caffeine 等实现只需要提供 `CacheService`。

PowerJob 自动注册对历史版本做了路径兜底：登录、应用列表、应用保存会优先使用新版接口，失败后自动尝试旧版路径；鉴权同时写入 `PowerJwt` 与 `Cookie`，兼容新旧管理端。保存和更新模型使用 PowerJob 官方 jar 内的 `tech.powerjob.common.request.http.SaveJobInfoRequest`，Solomon 只保留注解转换和历史字段补齐逻辑。

自动注册支持 `register-mode` 和 `failure-strategy`：

```yaml
powerjob:
  worker:
    register-mode: UPSERT
    failure-strategy: FAIL_FAST
    auto-create-namespace-app: true

xxl:
  register-mode: UPSERT
  failure-strategy: FAIL_FAST
  auto-resolve-job-group: true
  sync-status-on-update: false
```

## 目录结构

```text
solomon-parent/
├── solomon-mqtt-module/
├── solomon-s3-module/
│   ├── solomon-s3-sdk/
│   ├── solomon-minio/
│   ├── solomon-oss/
│   ├── solomon-obs/
│   ├── solomon-cos/
│   ├── solomon-bos/
│   └── solomon-amazon-s3/
├── solomon-job-module/
│   ├── solomon-job-sdk/
│   ├── solomon-powerjob/
│   └── solomon-xxlJob/
└── test-*/
```

## 构建

```bash
mvn clean install
```

构建任务调度聚合模块：

```bash
mvn -pl solomon-job-module -am clean install
```

构建对象存储聚合模块：

```bash
mvn -pl solomon-s3-module -am clean install
```

启用示例模块：

```bash
mvn -Ptest-modules clean test
```

## 依赖接入

### PowerJob

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-powerjob</artifactId>
  <version>1.0</version>
</dependency>
```

```yaml
powerjob:
  worker:
    enabled: true
    app-name: demo-service
    server-address: 127.0.0.1:7700

job:
  auto-register: true
  namespace: default
  user-name: admin
  password: 123456
```

### XXL-JOB

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-xxlJob</artifactId>
  <version>1.0</version>
</dependency>
```

```yaml
xxl:
  enabled: true
  auto-register: true
  admin-addresses: http://127.0.0.1:8080/xxl-job-admin
  app-name: demo-service
  user-name: admin
  password: 123456
```

XXL-JOB 自动注册使用 Admin 后台 HTTP 接口完成。`xxl-job-core` jar 内的 `AdminBizClient` 只支持执行器回调、注册和注销，不提供登录、保存、更新任务的管理端 Client；因此 Solomon 保留 `XxlJobInfo` 作为 Admin 表单模型，并对 `login/auth/doLogin`、`jobinfo/add/save/update/pageList/start/stop/remove` 做候选路径兼容。

### MinIO

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-s3-sdk</artifactId>
  <version>1.0</version>
</dependency>

<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-minio</artifactId>
  <version>1.0</version>
</dependency>
```

```yaml
file:
  choice: MINIO
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: default-bucket
  file-naming-method: UUID
```

### Amazon S3 / S3 兼容协议

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-s3-sdk</artifactId>
  <version>1.0</version>
</dependency>

<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-amazon-s3</artifactId>
  <version>1.0</version>
</dependency>
```

```yaml
file:
  choice: AMAZON
  endpoint: https://s3.amazonaws.com
  access-key: your-access-key
  secret-key: your-secret-key
  region-name: us-east-1
  bucket-name: default-bucket
  path-style-access-enabled: false
```

## 对象存储调用

推荐使用 `FileUploadRequest` 链式调用。

```java
FileUpload upload = fileService.upload(
    FileUploadRequest.multipart(file)
        .bucketName("default-bucket")
        .contentType("image/png")
        .metadata("bizId", "10001")
        .tag("source", "order")
        .overwrite(false)
        .useOriginalName(false)
);
```

```java
InputStream stream = fileService.download("demo.txt", "default-bucket");
String url = fileService.share(
    ShareFileRequest.file("default-bucket", "demo.txt")
        .expirySeconds(3600)
);
fileService.deleteFile("demo.txt", "default-bucket");
```

## 开发规范

- 公共注解、枚举、模型优先放入 `*-sdk` 模块。
- 实现模块只处理平台差异，不重复定义公共契约。
- 自动配置统一使用 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。
- 复杂逻辑必须有中文注释，注释解释设计意图，不重复代码含义。
- 禁止提交真实密码、token、Webhook、AccessKey、SecretKey。
- 提交前使用 JDK 21 完整构建。

## 常见问题

| 问题 | 常见原因 | 处理方式 |
| --- | --- | --- |
| 编译失败 | JDK 版本低于 21 | 切换到 JDK 21 |
| 找不到任务注解 | 只引入了实现模块旧缓存，或未刷新 Maven | 清理本地构建并重新导入 `solomon-job-module` |
| PowerJob 自动注册失败 | 管理端版本接口不一致、账号密码错误、地址错误 | 先确认 `job.*` 与 `powerjob.worker.*` 配置，再查看兜底路径日志 |
| XXL-JOB 未启动任务 | `scheduleType=NONE` 或 `start=false` | 配置调度类型和 `start=true` |
| 没有对象存储 Bean | 只引入了 SDK，未引入供应商模块，或 `file.choice` 不匹配 | 引入对应 `solomon-供应商` 模块并检查配置 |

## License

[Apache License 2.0](LICENSE)

## Author

- Author: steven
- Email: cao136623@163.com
