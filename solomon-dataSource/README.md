# solomon-dataSource

`solomon-dataSource` 是关系型数据库动态数据源模块，沿用当前工程 starter 风格，提供多租户数据源切换、Hikari/Druid 连接池、命名参数 SQL 执行、基础 ORM、普通分页和深度分页能力。

## 设计目标

- 自动配置入口：`SolomonDataSourceAutoConfiguration`
- 多租户上下文：基于 `DataSourceTenantContext` 保存当前线程租户编码
- 异常体系：通过 `DataSourceException extends BaseException` 抛出，错误文案走国际化
- 数据库：支持 MySQL、MariaDB、PostgreSQL、SQL Server、Oracle
- 连接池：支持 Hikari 和 Druid
- 租户模型：一个租户配置一个数据源，连接池、监控和安全配置跟随租户配置
- 路由模型：路由键就是租户编码，业务代码不按数据源名或 token 切换

## 使用方式

默认情况下，AOP 会在常见数据库访问入口前自动读取 `RequestHeaderHolder.getTenantCode()` 并切换租户数据源；如果当前上下文没有租户编码，则使用 `default-tenant`。

需要手动切换时，可以直接使用 `DataSourceTenantContext`。业务执行完成后必须清理线程上下文：

```java
dataSourceTenantContext.switchTenant("tenant-a");
try {
  // 执行业务查询
} finally {
  dataSourceTenantContext.clear();
}
```

## SQL执行

实体注解：

```java
@Table("sys_user")
public class SysUser {
  @PrimaryKey
  @Column("id")
  private Long id;

  @Column("name")
  private String name;
}
```

对象插入和更新：

```java
userRepository.insert(user);
userRepository.insert(userList);

userRepository.update(user);
userRepository.update(user, List.of("name", "status"));
userRepository.update(userList, List.of("name"));
```

普通连表查询：

```java
Sql sql = Sql.select("u.id", "u.name", "o.amount")
    .from("sys_user", "u")
    .leftJoin("sys_order", "o", "o.user_id = u.id")
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

常用条件语法：

```java
Sql sql = Sql.select("u.id", "u.name")
    .from("sys_user", "u")
    .eq("u.status", 1)
    .like("u.name", "%张%")
    .in("u.type", List.of("A", "B"))
    .between("u.created_time", startTime, endTime)
    .isNotNull("u.mobile")
    .orderByAsc("u.id");
```

连表聚合：

```java
Sql sql = Sql.select("u.id", "o.amount")
    .from("sys_user", "u")
    .leftJoin("sys_order", "o", "o.user_id = u.id")
    .eq("u.status", 1);

BigDecimal totalAmount = userRepository.query(sql).sum("amount");
long totalCount = userRepository.query(sql).count();
```

普通分页适合浅页：

```java
PageResult<UserVO> page = userRepository.query(
    Sql.select("u.id", "u.name").from("sys_user", "u")
).page(DataSourcePageParam.of(1, 20).desc("u.id").seekColumn("u.id"));
```

分页入口已经合并。业务侧继续调用 `page`，当页码和页大小达到配置阈值时，框架会自动切换为游标深分页。
如果没有传 `lastValue`，框架会先查询一次锚点游标值，再使用游标条件读取当前页数据。

基础分页参数：

```java
DataSourcePageParam param = DataSourcePageParam.of(1, 20)
    .desc("u.id")
    .asc("u.created_time")
    .seekColumn("u.id");

// 如果排序字段已经由后端白名单转换，也可以直接设置原始排序表达式。
param.setOrderBy("u.id DESC");
```

自动深分页配置：

```yaml
solomon:
  datasource:
    page:
      auto-seek-enabled: true
      seek-page-no: 500
      seek-page-size: 10
      default-seek-column: id
```

默认配置表示：`pageNo >= 500` 且 `pageSize >= 10` 时自动使用深度分页。
`default-seek-column` 可以按业务SQL配置成 `id`、`u.id` 或其他有索引且排序稳定的字段。

深度分页仍会按当前租户数据库方言生成 SQL：MySQL、MariaDB、PostgreSQL 使用 `LIMIT`，SQL Server 2005/2008 使用 `ROW_NUMBER`，SQL Server 2012+ 和 Oracle 12c+ 使用 `OFFSET/FETCH`。

分页方言按数据库拆分为独立实现：

- `MySqlDialect`
- `MariaDbDialect`
- `PostgreSqlDialect`
- `SqlServerDialect`：SQL Server 2005/2008，使用 `ROW_NUMBER`
- `SqlServer2012Dialect`：SQL Server 2012 及以上，使用 `OFFSET FETCH`
- `OracleDialect`
