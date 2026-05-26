# Solomon Parent

Solomon Parent 是基于 **Java 21**、**Spring Boot 3.4.4** 的微服务基础设施组件库。项目把常用基础能力拆成独立模块，统一管理依赖版本、自动配置、工具能力和中间件接入方式。

这个仓库不是单体应用。业务系统应按需引入模块，例如只接入 Redis、只接入 MinIO，或者只接入 MQTT 5，避免把所有中间件 SDK 一次性带入运行时。

## 快速信息

| 项目 | 内容 |
| --- | --- |
| GroupId | `com.steven` |
| Parent ArtifactId | `solomon-parent` |
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
| `solomon-xxlJob` | XXL-Job 执行器与任务创建封装 |
| `solomon-powerjob` | PowerJob Worker 与任务创建封装 |
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

### S3 / 对象存储

`solomon-s3-module` 借鉴 MQTT 聚合结构，拆成公共 SDK 与供应商实现模块。命名规则为 `solomon-供应商`：

| 子模块 | 职责 |
| --- | --- |
| `solomon-s3-sdk` | 公共接口、配置属性、上传模型、命名规则、抽象文件服务、图片工具 |
| `solomon-minio` | MinIO 实现 |
| `solomon-oss` | 阿里云 OSS 实现 |
| `solomon-obs` | 华为云 OBS 实现 |
| `solomon-cos` | 腾讯云 COS 实现 |
| `solomon-bos` | 百度云 BOS 实现 |
| `solomon-s3` | Amazon S3 及 S3 协议兼容实现，如 R2、TOS、KODO 等 |

业务项目通常引入 `solomon-s3-sdk` 加一个供应商模块即可。

## 目录结构

```text
solomon-parent/
├── docker/
├── solomon-constant/
├── solomon-utils/
├── solomon-base/
├── solomon-common/
├── solomon-datasource/
├── solomon-redis/
├── solomon-mongodb/
├── solomon-rabbitMq/
├── solomon-mqtt-module/
├── solomon-s3-module/
│   ├── solomon-s3-sdk/
│   ├── solomon-minio/
│   ├── solomon-oss/
│   ├── solomon-obs/
│   ├── solomon-cos/
│   ├── solomon-bos/
│   └── solomon-s3/
├── solomon-xxlJob/
├── solomon-powerjob/
├── solomon-gateway-sentinel/
├── solomon-bot-notice/
├── solomon-epc-coder/
└── test-*/
```

## 环境要求

| 环境 | 要求 |
| --- | --- |
| JDK | 21 |
| Maven | 3.9+ 推荐 |
| Spring Boot 业务项目 | 3.x |

## 构建

完整构建：

```bash
mvn clean install
```

构建对象存储聚合模块：

```bash
mvn -pl solomon-s3-module -am clean install
```

构建某个供应商模块：

```bash
mvn -pl solomon-s3-module/solomon-minio -am clean install
```

启用示例模块：

```bash
mvn -Ptest-modules clean test
```

## 依赖接入

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
  part-size: 5
```

### 阿里云 OSS

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-s3-sdk</artifactId>
  <version>1.0</version>
</dependency>

<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-oss</artifactId>
  <version>1.0</version>
</dependency>
```

```yaml
file:
  choice: OSS
  endpoint: https://oss-cn-hangzhou.aliyuncs.com
  access-key: your-access-key
  secret-key: your-secret-key
  bucket-name: your-bucket
```

### 华为云 OBS / 腾讯云 COS / 百度云 BOS

按供应商引入对应模块：

```xml
<artifactId>solomon-obs</artifactId>
<artifactId>solomon-cos</artifactId>
<artifactId>solomon-bos</artifactId>
```

配置项仍使用统一前缀 `file.*`，通过 `file.choice` 选择供应商：`OBS`、`COS`、`BOS`。

### S3 协议兼容存储

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-s3-sdk</artifactId>
  <version>1.0</version>
</dependency>

<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-s3</artifactId>
  <version>1.0</version>
</dependency>
```

```yaml
file:
  choice: S3
  endpoint: https://s3.example.com
  access-key: your-access-key
  secret-key: your-secret-key
  region-name: us-east-1
  bucket-name: default-bucket
  path-style-access-enabled: true
```

## 对象存储调用

`FileServiceInterface` 是统一入口。旧的重载方法继续可用，推荐新代码使用 `FileUploadRequest` 做链式调用。

```java
FileUpload upload = fileService.upload(
    FileUploadRequest.multipart(file)
        .bucketName("default-bucket")
        .useOriginalName(false)
);
```

上传输入流：

```java
FileUpload upload = fileService.upload(
    FileUploadRequest.stream(inputStream, "demo.txt")
        .bucketName("default-bucket")
);
```

下载、分享、删除：

```java
InputStream stream = fileService.download("demo.txt", "default-bucket");
String url = fileService.share("demo.txt", "default-bucket", 3600);
fileService.deleteFile("demo.txt", "default-bucket");
```

生成缩略图：

```java
InputStream thumbnail = fileService.generateThumbnail(
    "default-bucket",
    "demo.jpg",
    "thumbnail/",
    true,
    200,
    200
);
```

## 自动配置约定

- 公共 SDK 自动配置：`S3SdkAutoConfig`。
- 供应商自动配置：`MinioAutoConfig`、`OssAutoConfig`、`ObsAutoConfig`、`CosAutoConfig`、`BosAutoConfig`、`S3AutoConfig`。
- 所有自动配置通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 加载。
- 供应商模块通过 `file.choice` 精确启用。
- `FileServiceInterface` 使用 `@ConditionalOnMissingBean`，业务项目可以覆盖默认实现。

## 其他核心能力

### OpenAPI / Knife4j

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

### Redis

```yaml
spring:
  redis:
    enabled: true
    host: localhost
    port: 6379
```

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

### MQTT

```yaml
mqtt:
  enabled: true
```

### 机器人通知

```yaml
solomon:
  notice:
    enabled: true
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

### GS1 EPC 编解码

```java
EpcResult result = epcService.gs1()
    .ai01("06901234567892")
    .ai21("1234567890")
    .companyPrefixLength(6)
    .tagSize(96)
    .encode();
```

## 配置速查

| 配置项 | 说明 |
| --- | --- |
| `file.choice` | 对象存储供应商，如 `MINIO`、`OSS`、`OBS`、`COS`、`BOS`、`S3` |
| `file.endpoint` | 对象存储服务地址 |
| `file.access-key` | 访问密钥 |
| `file.secret-key` | 私密密钥 |
| `file.bucket-name` | 默认 Bucket |
| `file.root-directory` | 对象名前缀 |
| `file.region-name` | 区域 |
| `file.file-naming-method` | 文件命名规则 |
| `file.part-size` | 分片大小，单位 MB |
| `file.connection-timeout` | 连接超时，单位毫秒 |
| `file.socket-timeout` | 读取超时，单位毫秒 |
| `file.path-style-access-enabled` | S3 是否使用 path-style 访问 |

## 开发规范

- 公共接口、模型、抽象服务、命名规则放在 `solomon-s3-sdk`。
- 供应商实现按 `solomon-供应商` 命名，禁止把所有 SDK 混在一个模块里。
- 供应商模块只处理平台差异，不重复实现上传调度、命名、缩略图等公共逻辑。
- 新增供应商时必须补充自动配置和 `AutoConfiguration.imports`。
- 中文注释用于解释设计意图和复杂逻辑。
- 不提交真实密码、token、Webhook、AccessKey、SecretKey。
- 提交前使用 JDK 21 完整构建。

## 常见问题

| 问题 | 常见原因 | 处理方式 |
| --- | --- | --- |
| 编译失败 | JDK 版本低于 21 | 切换到 JDK 21 |
| 没有 `FileServiceInterface` Bean | 只引入了 SDK，未引入供应商模块，或 `file.choice` 不匹配 | 引入对应 `solomon-供应商` 模块并检查配置 |
| 供应商 SDK 冲突 | 一次性引入多个供应商模块 | 业务项目只引入当前需要的供应商模块 |
| 上传失败 | endpoint、region、密钥、bucket 配置错误 | 先用供应商控制台验证配置 |
| ClamAV 扫描失败 | ClamAV 未部署或连接配置错误 | 检查 `clamav.*` 配置 |

## License

[Apache License 2.0](LICENSE)

## Author

- Author: steven
- Email: cao136623@163.com
