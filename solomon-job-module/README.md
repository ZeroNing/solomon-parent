# Solomon Job Module

`solomon-job-module` 聚合 PowerJob、XXL-JOB 与公共 Job SDK。公共注解和枚举放在 `solomon-job-sdk`，平台差异由 `solomon-powerjob`、`solomon-xxlJob` 各自处理。

## 模块

| 模块 | 说明 |
| --- | --- |
| `solomon-job-sdk` | 统一 `JobTask`、平台枚举、自动注册模式、失败策略、日志脱敏工具 |
| `solomon-powerjob` | PowerJob Worker、自动注册、OpenAPI/历史后台接口兼容 |
| `solomon-xxlJob` | XXL-JOB Executor、自动注册、历史/新版 Admin 接口兼容 |

## 自动注册策略

| 配置值 | 说明 |
| --- | --- |
| `UPSERT` | 不存在则创建，存在则更新 |
| `CREATE_ONLY` | 只创建不存在的任务，存在则跳过 |
| `UPDATE_ONLY` | 只更新已存在任务，不存在则跳过 |

失败策略：

| 配置值 | 说明 |
| --- | --- |
| `FAIL_FAST` | 自动注册失败时中断应用启动 |
| `WARN_ONLY` | 自动注册失败时只记录警告，应用继续启动 |

## PowerJob

```yaml
powerjob:
  worker:
    enabled: true
    app-name: demo-service
    server-address: 127.0.0.1:7700
    user-name: admin
    password: 123456
    namespace: default
    auto-register: true
    register-mode: UPSERT
    failure-strategy: FAIL_FAST
    auto-create-namespace-app: true
```

PowerJob 优先使用 jar 内 `OpenAPIConstant` 拼接官方 OpenAPI 路径，并使用官方 `SaveJobInfoRequest` 保存、更新任务。历史版本没有对应 OpenAPI 时，会回退到旧后台接口。

## XXL-JOB

```yaml
xxl:
  enabled: true
  auto-register: true
  register-mode: UPSERT
  failure-strategy: FAIL_FAST
  auto-resolve-job-group: true
  sync-status-on-update: false
  admin-addresses: http://127.0.0.1:8080/xxl-job-admin
  app-name: demo-service
  user-name: admin
  password: 123456
```

XXL-JOB core jar 内的 `AdminBizClient` 只支持执行器回调和注册，不提供后台登录、保存、更新任务接口。因此自动注册通过 Admin HTTP 接口完成，并兼容 `login/auth/doLogin`、`jobinfo/add/save/update/pageList/start/stop/remove` 等路径。

## 注解示例

```java
@JobTask(
    platforms = {JobPlatform.POWERJOB},
    taskName = "库存同步",
    timeExpressionType = TimeExpressionType.CRON,
    timeExpression = "0 0/5 * * * ?"
)
public class StockSyncProcessor implements BasicProcessor {
}
```

```java
@JobTask(
    platforms = {JobPlatform.XXL_JOB},
    taskName = "订单关闭",
    scheduleType = ScheduleTypeEnum.CRON,
    scheduleConf = "0 0/1 * * * ?",
    executorHandler = "orderCloseJob",
    start = true
)
public class OrderCloseHandler extends AbstractJobConsumer {
}
```

## 设计约定

- 自动注册默认不会打印明文密码、token、cookie、secret。
- XXL-JOB 默认按 `xxl.app-name` 自动解析执行器组 ID，查不到时回退注解 `jobGroup`。
- 自动注册会比较任务参数，无变化时跳过更新，减少 Admin 写请求。
- 生产环境建议使用 `CREATE_ONLY` 或 `WARN_ONLY`，避免启动时意外覆盖人工调整的任务。
