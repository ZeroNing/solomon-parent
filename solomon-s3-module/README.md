# Solomon S3 Module

`solomon-s3-module` 是对象存储聚合模块，采用公共 SDK + 供应商实现的结构。业务项目通常引入 `solomon-s3-sdk` 和一个供应商模块即可。

## 模块

| 模块 | 说明 |
| --- | --- |
| `solomon-s3-sdk` | 公共接口、上传请求、分享请求、分片模型、配置属性、命名规则、图片工具 |
| `solomon-minio` | MinIO 实现 |
| `solomon-oss` | 阿里云 OSS 实现 |
| `solomon-obs` | 华为云 OBS 实现 |
| `solomon-cos` | 腾讯云 COS 实现 |
| `solomon-bos` | 百度云 BOS 实现 |
| `solomon-amazon-s3` | Amazon S3 及 S3 协议兼容实现 |

## 依赖

业务项目只需要引入公共 SDK 和一个供应商实现。Amazon S3 或 S3 协议兼容供应商使用 `solomon-amazon-s3`：

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

## 配置

```yaml
file:
  choice: MINIO
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: default-bucket
  file-naming-method: UUID
  auto-create-bucket: true
  check-bucket-on-startup: false
```

供应商模块会校验 `endpoint`、`access-key`、`secret-key`、`bucket-name`。启用 `check-bucket-on-startup` 后，会在 Bean 初始化阶段检查默认桶；桶不存在且 `auto-create-bucket=true` 时会自动创建。

## 上传

```java
FileUpload upload = fileService.upload(
    FileUploadRequest.multipart(file)
        .bucketName("default-bucket")
        .contentType("image/png")
        .metadata("bizId", "10001")
        .tag("source", "order")
        .overwrite(false)
);
```

## 分享

```java
String url = fileService.share(
    ShareFileRequest.file("default-bucket", "demo.png")
        .expirySeconds(3600)
        .downloadFileName("demo.png")
);
```

## 能力声明

每个实现通过 `capabilities()` 暴露当前供应商能力，例如普通上传、下载、预签名、分片上传、桶管理、复制对象、缩略图等。公共服务在调用能力前会先校验，避免进入供应商 SDK 后才抛出晦涩异常。

## 设计约定

- `solomon-amazon-s3` 使用 AWS SDK v2，对 Amazon S3 和兼容 S3 协议的供应商复用同一实现。
- 图片处理能力保留在 SDK 的工具包内，上传链路只在显式调用缩略图接口时进入图片处理。
- 新增供应商时先复用 `FileChoiceProperties`、`FileUploadRequest`、`ShareFileRequest`，供应商模块只处理 SDK 差异。
