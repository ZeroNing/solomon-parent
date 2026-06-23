# Solomon Starter Contracts

This document records the minimum runtime contract for the framework starters. It is intentionally operational:
configuration that must exist, metrics that should be scraped, and migration notes that avoid silent production
misconfiguration.

## Configuration Validation

All starter-facing `@ConfigurationProperties` classes must use `@Validated` and fail fast with clear messages.
The current scan covers every `@ConfigurationProperties` class in this repository. Shared tenant maps validate blank
tenant keys and recursively validate tenant-specific values.

| Module | Prefix | Required or guarded fields |
| --- | --- | --- |
| Security | `security.jwt` | `key-id`, `secret` or `secrets[key-id]`, `issuer`, token TTLs, clock skew |
| Gateway | `gateway.*` | tenant/security/gray/swagger path lists and forwarding switches |
| Common | `solomon.swagger`, `tenant` | OpenAPI defaults, global parameter shape, tenant mode |
| Cache | `cache.redis`, `cache.caffeine` | key mode, tenant routing, default tenant, Redis/Caffeine limits |
| Cache SDK | `cache.key`, `cache.repeat-request` | key prefix and repeat request TTL |
| MQ/MQTT | `spring.rabbitmq`, `rabbitmq`, `rocketmq`, `mqtt`, `solomon.mq.idempotency` | broker toggles, tenant map keys, nested tenant configuration, idempotency TTLs |
| S3 | `file` | provider choice, endpoint, access key, secret key, bucket, timeout, part size |
| Notice | `solomon.notice` | webhook URLs, bounded executor, send timeout, retry backoff |
| Job | `xxl.*`, `powerjob.*` | admin/app names, access token, timeout, registration retry/backoff/circuit settings |
| Persistence | `persistence` | default tenant, tenant datasource URL, pagination bounds, script table |
| MongoDB | `spring.data.mongodb` | tenant map keys and switch mode |

When `file.choice` is not `DEFAULT`, `file.endpoint`, `file.access-key`, `file.secret-key`, and
`file.bucket-name` are required. JWT no longer has a default shared signing key; deployments must configure
their own active key and keep old keys in `security.jwt.secrets` during rotation.

## Minimum Examples

### JWT

```yaml
security:
  jwt:
    key-id: key-2026-06
    secrets:
      key-2026-06: ${JWT_SECRET_CURRENT}
      key-2026-03: ${JWT_SECRET_PREVIOUS}
    issuer: solomon
    expire-seconds: 7200
    refresh-expire-seconds: 2592000
    clock-skew-seconds: 60
```

### Redis Cache

```yaml
cache:
  redis:
    enabled: true
    key:
      mode: TENANT_PREFIX
      prefix: solomon
```

### Local Cache

```yaml
cache:
  caffeine:
    enabled: true
    maximum-size: 10000
    default-expire: 30m
    key:
      mode: TENANT_PREFIX
      prefix: solomon
```

Enable either Redis or Caffeine intentionally for each application profile. When both are present, document which
`CacheService` bean each business component injects and avoid depending on by-type injection when multiple cache
implementations are active.

### Tenant And OpenAPI

```yaml
tenant:
  mode: SINGLE
  default-code: default
  require-code-in-multi-mode: true

solomon:
  swagger:
    enabled: true
    title: Solomon API
    version: 1.0.0
    global-request-parameters:
      - name: tenantCode
        in: header
        description: Tenant code
        required: false
```

Set `tenant.mode=MULTI` only after Servlet/WebFlux ingress, Feign, Dubbo, MQ consumers, and async executors are all
using the provided context propagation helpers. In multi-tenant mode, missing tenant headers should be treated as a
configuration or caller error unless the endpoint is explicitly public.

### Object Storage

```yaml
file:
  choice: MINIO
  endpoint: http://localhost:9000
  access-key: ${S3_ACCESS_KEY}
  secret-key: ${S3_SECRET_KEY}
  bucket-name: solomon
  file-naming-method: UUID
  part-size: 5
  connection-timeout: 60000
  socket-timeout: 60000
```

Only one concrete object storage provider starter should normally be active in a service. If several provider modules
are on the classpath for a shared platform build, configure exactly one `file.choice` and override the
`FileServiceInterface` bean explicitly when a service needs custom routing.

### MQ Idempotency

```yaml
spring:
  rabbitmq:
    enabled: true
rocketmq:
  enabled: true

solomon:
  mq:
    idempotency:
      enabled: true
      processing-ttl: 10m
      consumed-ttl: 1d
```

The starter only creates an in-memory `MessageIdempotencyStore` when no custom store exists. Multi-node
deployments should provide a Redis or database-backed `MessageIdempotencyStore`.

### Notice

```yaml
solomon:
  notice:
    enabled: true
    global-signature: true
    signature: "[Solomon]"
    async-core-pool-size: 2
    async-max-pool-size: 8
    async-queue-capacity: 200
    send-timeout-millis: 5000
    retry-backoff-millis: 2000
    ding-talk:
      webhook-url: ${DING_TALK_WEBHOOK}
      secret: ${DING_TALK_SECRET:}
```

The async executor is bounded. Size the queue and rejection behavior according to notification criticality; production
services should alert on failed sends instead of silently treating notification delivery as best effort.

### Job Registration

```yaml
xxl:
  enabled: true
  admin-addresses: http://xxl-admin:8080/xxl-job-admin
  app-name: solomon-app
  access-token: ${XXL_ACCESS_TOKEN:}
  register:
    enabled: false
    user-name: ${XXL_ADMIN_USER:}
    password: ${XXL_ADMIN_PASSWORD:}
    mode: UPSERT
    failure-strategy: FAIL_FAST
    admin-api-max-attempts: 3
    admin-api-backoff-millis: 1000
    admin-api-circuit-failure-threshold: 5
    admin-api-circuit-open-duration-millis: 30000

powerjob:
  worker:
    register:
      enabled: false
      namespace: ${POWERJOB_NAMESPACE:}
      user-name: ${POWERJOB_ADMIN_USER:}
      password: ${POWERJOB_ADMIN_PASSWORD:}
      mode: UPSERT
      failure-strategy: FAIL_FAST
      admin-api-max-attempts: 3
      admin-api-backoff-millis: 1000
```

Keep automatic registration disabled until the service account, namespace, and admin API reachability are verified in
the target environment. When enabled, health checks authenticate against the admin API and registration calls use the
configured retry/backoff/circuit settings.

### Persistence And MongoDB

```yaml
persistence:
  enabled: true
  default-tenant: default
  tenants:
    default:
      url: jdbc:mysql://localhost:3306/solomon
      username: ${DB_USER}
      password: ${DB_PASSWORD}
  page:
    auto-seek-enabled: true
    seek-page-no: 500
    seek-page-size: 10
    default-seek-column: id
  script:
    record-table: solomon_schema_script

spring:
  data:
    mongodb:
      mode: NORMAL
      tenant:
        default:
          uri: mongodb://localhost:27017/solomon
```

Tenant map keys must not be blank. Roll out script execution with checksum review first; once a script id has run,
changing its body should be treated as a migration error and delivered as a new script id.

### MQTT

```yaml
mqtt:
  enabled: true
  tenant:
    default:
      url: tcp://localhost:1883
      user-name: ${MQTT_USER:}
      password: ${MQTT_PASSWORD:}
      client-id: solomon-app
      automatic-reconnect: true
      clean-session: false
```

MQTT modules share the same tenant routing contract as the rest of the framework. Confirm which concrete MQTT provider
module is on the classpath; do not include multiple provider starters in one service unless the tenant routing strategy
requires it and bean selection is explicit. MQTT3, MQTT5, and Vert.x MQTT complete asynchronous publish futures with the
captured request context, so synchronous `CompletableFuture` continuations keep the original request id and tenant.

## Observability Contract

Scrape these metric families when Micrometer is present:

| Area | Metric | Tags |
| --- | --- | --- |
| Redis cache | `solomon.cache.redis.operation.total` / `.duration` | `operation`, `group`, `outcome` |
| Caffeine cache | `solomon.cache.caffeine.operation.total` / `.duration` | `operation`, `group`, `outcome` |
| Notice | `solomon.notice.send.total` / `.duration` | `channel`, `async`, `outcome` |
| RocketMQ | `solomon.mq.rocketmq.send.total` / `.duration` | `operation`, `topic`, `outcome` |
| RabbitMQ | `solomon.mq.rabbitmq.send.total` / `.duration` | `operation`, `exchange`, `routingKey`, `outcome` |
| Object storage | `solomon.s3.operation.total` / `.duration` | `provider`, `operation`, `bucket`, `outcome` |
| Job admin | `solomon.job.admin.request.total` / `.duration` | `provider`, `method`, `path`, `outcome` |
| Tenant switch | `solomon.tenant.switch.total` / `.duration` | `tenant`, `binder`, `phase`, `outcome` |

Health indicators are exposed for Redis cache, Caffeine cache, RabbitMQ broker connectivity, RocketMQ producer state,
object storage provider activation, and job admin connectivity. RabbitMQ health opens and closes a broker connection
when the starter is enabled, and reports UP without connecting when `spring.rabbitmq.enabled=false`. RocketMQ health
reads the underlying producer `ServiceState` and does not publish probe messages. Object storage health reports DOWN
when only the default placeholder service is active, and UP when a concrete provider service is present with a
non-DEFAULT provider choice. Job admin health checks report UP when automatic registration is disabled, and
authenticate against the admin API only when registration is enabled.
Tenant resource switching emits metrics through `TenantResourceScope` when a `TenantSwitchObserver` is registered;
`solomon-common-module` auto-registers a Micrometer observer when `MeterRegistry` is present.

## Reliability Contract

- Use `RetryExecutor` for bounded retries around external calls that are safe to retry. Defaults must preserve
  previous behavior with one attempt unless a starter property opts in to retry.
- Use `CircuitBreaker` around external calls that may amplify outages. The common implementation opens after a
  configurable consecutive-failure threshold, fails fast during the open window, then performs a half-open probe.
- MQ consumers can use `MessageIdempotencyExecutor` with a pluggable `MessageIdempotencyStore` to guard `msgId`
  processing. Set `solomon.mq.idempotency.enabled=true` for starter auto-configuration; the default in-memory
  store is for tests and single-node demos, and production should provide Redis or database-backed storage.
- Job admin registration calls support `admin-api-max-attempts`, `admin-api-backoff-millis`,
  `admin-api-circuit-failure-threshold`, and `admin-api-circuit-open-duration-millis` under `xxl.register`
  and `powerjob.worker.register`.
- MQ consumers should keep provider-native retry/DLQ behavior explicit; RabbitMQ retry annotations map to Spring
  Retry and DLX bindings. Set `spring.rabbitmq.reliability.require-dlx-for-retry=true` to fail startup when a retry
  listener omits `MessageListener.dlxClazz`.

Key logs for authentication, MQ, cache and storage operations should include stable fields such as `tenant`,
`userId`, `requestId`, `topic`, `msgId`, `bucket`, `operation`, `path`, and `outcome`.

## Security And Context Migration Notes

- JWT access tokens and refresh tokens now carry a token type and `jti`; refresh rotation revokes the old refresh
  token. Production deployments should replace the default in-memory revocation store with Redis or a database.
- Permission metadata stores expose `invalidateAll`, `invalidateByCode`, `invalidateByPath`, and `version`.
  Custom `AccessValidator` implementations should use this version to invalidate local permission decision caches.
- Request context propagation is explicit. Use the provided Spring `TaskDecorator`, `RequestContextSnapshot`, and
  `ContextAwareCompletableFuture` helpers for thread pools and asynchronous callbacks.
- Servlet and WebFlux ingress filters bind `requestId`, timezone, tenant code, tenant id, and tenant name from
  headers. Feign and Dubbo propagate the same fields to downstream services.
- MQ consumers bind tenant/request context from message metadata and restore the previous context after handling.
- Roll out context propagation before enabling multi-tenant resource switching. A good migration order is ingress
  filters first, then Feign/Dubbo outbound propagation, then MQ consumers, then executor `TaskDecorator` and
  `ContextAwareCompletableFuture` adoption.
- Existing JWT deployments must migrate from the removed default secret by first configuring `security.jwt.key-id`
  and `security.jwt.secrets[current]`, then adding previous signing keys to the map until old tokens naturally expire.
- Existing notification deployments should review `solomon.notice.async-*`; the previous unbounded cached executor
  behavior is replaced by bounded pools and can now reject overload instead of expanding threads indefinitely.
- Existing RabbitMQ retry listeners should set `spring.rabbitmq.reliability.require-dlx-for-retry=true` in staging to
  surface listeners that retry without a dead-letter consumer before enforcing it in production.

## Dependency And Runtime Notes

- Do not include multiple object storage provider starters unless the application explicitly chooses one
  `FileServiceInterface` bean.
- `solomon-amazon-s3` uses AWS SDK v2 and is the preferred module for Amazon S3 or S3-compatible providers.
- Avoid including multiple concrete MQ or MQTT provider starters unless bean selection is explicit. The common SDKs
  define contracts; provider modules contribute concrete sender/consumer implementations and may otherwise create
  ambiguous by-type injections.
- Prefer one cache implementation per application profile. If Redis and Caffeine are both active, inject by bean name
  or wrap them behind a service-local cache facade.
- RocketMQ health reads producer state only. It does not publish probe messages, so it proves local producer startup
  state rather than topic-level broker write availability.
- Mockito may emit dynamic agent warnings on JDK 21 during tests. This does not affect runtime, but the build should
  eventually configure Mockito as a JVM agent to prepare for future JDK defaults.
- `spring-configuration-metadata.json` is generated by the root `maven-compiler-plugin` annotation processor setup
  for modules that expose `@ConfigurationProperties`; key security/reliability properties also provide
  `additional-spring-configuration-metadata.json` entries for rotation, DLQ, retry and circuit-breaker contracts.
