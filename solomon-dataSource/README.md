# solomon-datasource

`solomon-datasource` 是 Solomon 的关系型数据库动态数据源和轻量 ORM 模块，支持多租户数据源路由、Hikari/Druid 连接池、命名参数 SQL、对象插入更新、分页、深度分页和可扩展类型转换器。

## 能力概览

- 支持数据库：MySQL、MariaDB、PostgreSQL、SQL Server、Oracle。
- 支持连接池：Hikari、Druid。
- 支持多租户：一个租户一个数据源，通过租户编码切换。
- 支持自动切换：Repository 和 SqlExecutor 调用时会根据当前租户上下文切换数据源。
- 支持 SQL 方言：分页语法按数据库类型自动生成，SQL Server 区分 2005/2008 与 2012+。
- 支持 ORM 注解：`@Table`、`@Column`、`@PrimaryKey`。
- 支持对象写入：单对象、集合、数组插入和更新，更新可指定字段。
- 支持分页：普通分页和深度分页入口合并，由配置控制自动切换。
- 支持类型转换器：不配置时使用默认转换器，也可以通过 `addConverter` 添加业务转换器。
- 异常体系：统一抛出 `DataSourceException`，继承项目 `BaseException`，错误文案走 i18n。

## Maven 依赖

```xml
<dependency>
  <groupId>com.steven</groupId>
  <artifactId>solomon-datasource</artifactId>
  <version>1.0</version>
</dependency>
```

业务模块如果需要 Web、Swagger、全局异常处理，可以像测试模块一样引入 `solomon-common`。

## 数据源配置

```yaml
solomon:
  datasource:
    enabled: true
    default-tenant: default
    page:
      auto-seek-enabled: true
      seek-page-no: 500
      seek-page-size: 10
      default-seek-column: id
    tenants:
      default:
        pool-type: HIKARI
        database-type: MYSQL
        url: jdbc:mysql://127.0.0.1:3306/demo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
        username: root
        password: root
        hikari:
          maximum-pool-size: 10
          minimum-idle: 2
          pool-name: solomon-datasource-default
      tenant-a:
        pool-type: DRUID
        database-type: POSTGRESQL
        url: jdbc:postgresql://127.0.0.1:5432/demo
        username: postgres
        password: postgres
        druid:
          initial-size: 2
          min-idle: 2
          max-active: 10
```

## 租户切换

框架会优先从当前请求上下文读取租户编码；没有租户编码时使用 `default-tenant`。

手动切换示例：

```java
dataSourceTenantContext.switchTenant("tenant-a");
try {
  List<Tenant> tenants = tenantRepository.findAll();
} finally {
  dataSourceTenantContext.clear();
}
```

## 实体注解

`@PrimaryKey` 只用于标记主键；字段列名由 `@Column` 指定。如果 `@Column` 不填值，会自动把 Java 驼峰字段转为下划线列名，例如 `userId -> user_id`。

```java
@Table("sys_user")
public class SysUser {

  @PrimaryKey
  @Column
  private Long id;

  @Column
  private String userName;

  @Column
  private LocalDateTime createTime;
}
```

## Repository 用法

```java
public class UserRepository extends BaseRepository<SysUser> {

  public UserRepository(SqlExecutor sqlExecutor) {
    super(sqlExecutor);
  }
}
```

常用查询：

```java
SysUser user = userRepository.getById(1L);
List<SysUser> users = userRepository.findByField("status", 1);
long total = userRepository.count();
```

插入和更新：

```java
userRepository.insert(user);
userRepository.insert(userList);

userRepository.update(user);
userRepository.update(user, List.of("userName", "status"));
userRepository.update(userList, List.of("status"));
```

## SQL 构建

兼容手写 SQL：

```java
Sql sql = Sql.New("select * from (");
sql.append("select u.*, d.name as dept_name from sys_user u ");
sql.append("left join sys_dept d on d.id = u.dept_id ");
sql.where().and(Cond.eq("u.id", userId, false));
sql.append(") t ").orderBy(param.orderBy());

return userRepository.findPageLite(sql, param);
```

结构化 SQL：

```java
Sql sql = Sql.select("u.id", "u.user_name", "d.name as dept_name")
    .from("sys_user", "u")
    .leftJoin("sys_dept", "d")
    .on("d.id = u.dept_id")
    .on("d.deleted = 0")
    .eq("u.status", 1)
    .orderByDesc("u.id");

List<UserVO> list = userRepository.query(sql).list();
```

分组和 HAVING：

```java
Sql sql = Sql.select("u.dept_id", "COUNT(1) AS user_count", "SUM(o.amount) AS amount")
    .from("sys_user", "u")
    .leftJoin("sys_order", "o", "o.user_id = u.id")
    .groupBy("u.dept_id")
    .havingGt("SUM(o.amount)", BigDecimal.ZERO);
```

## 分页和深度分页

分页统一使用 `page` 方法。达到配置阈值时自动切换到深度分页。

```java
PageResult<UserVO> page = userRepository.query(sql)
    .page(DataSourcePageParam.of(10000, 10000)
        .desc("u.id")
        .seekColumn("u.id"));
```

返回值包含：

- `records`：当前页数据。
- `total`：总数。
- `hasNext`：是否有下一页。
- `seekPage`：是否使用深度分页。
- `nextSeekValue`：下一页游标值。

## 类型转换器

不配置时会使用默认转换器，默认支持 `LocalDateTime`、`LocalDate`、`LocalTime`、`Date`、`Long`、常见数字类型、`Boolean`、`Enum` 等。

业务模块可以通过 `SqlTypeConverterCustomizer` 添加转换器：

```java
@Configuration
public class DataSourceConverterConfig {

  @Bean
  public SqlTypeConverterCustomizer sqlTypeConverterCustomizer() {
    return registry -> registry.addConverter(new SqlValueConverter() {

      @Override
      public boolean supportsJava(Object value, Class<?> targetType) {
        return targetType == String.class && value instanceof java.sql.Timestamp;
      }

      @Override
      public Object convertForJava(Object value, Class<?> targetType) {
        return ((java.sql.Timestamp) value)
            .toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      }

      @Override
      public boolean supportsJdbc(Object value) {
        return false;
      }

      @Override
      public Object convertForJdbc(Object value) {
        return value;
      }
    });
  }
}
```

## Swagger 测试模块配置

测试模块统一使用 Springdoc + Knife4j。`solomon.swagger` 负责标题和全局请求头，`springdoc` 负责接口文档路径和扫描包，`knife4j` 负责增强 UI。

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
  group-configs:
    - group: default
      paths-to-match: /**
      packages-to-scan: com.steven
knife4j:
  enable: true
  setting:
    language: zh_cn
solomon:
  swagger:
    enabled: true
    title: datasource测试用例
    version: 1.0.0
    global-request-parameters:
      - name: token
        in: header
        description: 用户认证令牌
        required: true
        hidden: false
```

启动测试模块后访问：

- Swagger UI：`http://localhost:8001/swagger-ui.html`
- Knife4j UI：`http://localhost:8001/doc.html`
- OpenAPI JSON：`http://localhost:8001/v3/api-docs`

## 编译验证

```bash
mvn -Ptest-modules -pl test-solomon-datasource -am -DskipTests compile
```
