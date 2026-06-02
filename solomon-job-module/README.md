# Solomon Job Module

`solomon-job-module` 聚合 PowerJob、XXL-JOB 与公共 Job SDK。公共任务参数保留在 `JobTask`，平台差异参数分别放在 `PowerJobTask` 和 `XxlJobTask`。

## 模块

| 模块 | 说明 |
| --- | --- |
| `solomon-job-sdk` | 统一任务注解、平台枚举、自动注册模式与失败策略 |
| `solomon-powerjob` | PowerJob Worker、自动注册、OpenAPI 与历史后台接口兼容 |
| `solomon-xxlJob` | XXL-JOB Executor、自动注册、历史与新版 Admin 接口兼容 |

## 配置分区

启动必备参数与后台自动注册参数分开配置。只需要启动 Worker 或 Executor 时，可以完全省略 `register` 配置。

### PowerJob

PowerJob Worker 启动参数沿用官方 starter 的 `powerjob.worker` 配置；后台登录和自动注册参数放在 `powerjob.worker.register`。

```yaml
powerjob:
  worker:
    enabled: true
    app-name: demo-service
    server-address: 127.0.0.1:7700
    register:
      enabled: true
      user-name: admin
      password: 123456
      namespace: default
      mode: UPSERT
      failure-strategy: FAIL_FAST
      auto-create-namespace-app: true
```

### XXL-JOB

XXL-JOB Executor 启动参数放在 `xxl`；后台登录和自动注册参数放在 `xxl.register`。

```yaml
xxl:
  enabled: true
  admin-addresses: http://127.0.0.1:8080/xxl-job-admin
  app-name: demo-service
  address:
  ip:
  port: 9999
  access-token:
  register:
    enabled: true
    user-name: admin
    password: 123456
    mode: UPSERT
    failure-strategy: FAIL_FAST
    auto-resolve-job-group: true
    sync-status-on-update: false
```

## 注解示例

PowerJob 专属参数放在 `powerJob`：

```java
@JobTask(
    platforms = {JobPlatform.POWERJOB},
    taskName = "库存同步",
    powerJob = @PowerJobTask(
        timeExpressionType = TimeExpressionType.CRON,
        timeExpression = "0 0/5 * * * ?"
    )
)
public class StockSyncProcessor implements BasicProcessor {
}
```

XXL-JOB 专属参数放在 `xxlJob`：

```java
@JobTask(
    platforms = {JobPlatform.XXL_JOB},
    taskName = "订单关闭",
    xxlJob = @XxlJobTask(
        scheduleType = ScheduleTypeEnum.CRON,
        scheduleConf = "0 0/1 * * * ?",
        executorHandler = "orderCloseJob",
        start = true
    )
)
public class OrderCloseHandler extends AbstractJobConsumer {
}
```

## 自动注册策略

| 配置值 | 说明 |
| --- | --- |
| `UPSERT` | 不存在则创建，存在则更新 |
| `CREATE_ONLY` | 只创建不存在的任务 |
| `UPDATE_ONLY` | 只更新已经存在的任务 |

失败策略：

| 配置值 | 说明 |
| --- | --- |
| `FAIL_FAST` | 自动注册失败时中断应用启动 |
| `WARN_ONLY` | 自动注册失败时记录警告并继续启动 |

自动注册会比较任务参数，无变化时跳过更新，减少 Admin 写请求。生产环境建议按需使用 `CREATE_ONLY` 或 `WARN_ONLY`，避免启动时意外覆盖人工调整的任务。
