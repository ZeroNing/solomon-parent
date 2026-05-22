package com.steven.solomon.datasource.sql.script;

import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.enums.DataBaseTypeEnum;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties.SingleDataSourceProperties;
import com.steven.solomon.datasource.routing.DataSourceTenantContext;
import com.steven.solomon.datasource.sql.SqlInjectionGuard;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * SQL脚本执行工具。
 *
 * <p>该工具会在当前租户数据源中维护一张脚本执行记录表，执行脚本前先检查脚本编码是否已经存在。
 * 已执行过的脚本会自动跳过，避免重复执行建表、初始化数据或升级脚本。</p>
 */
public class SqlScriptExecutor {

  private static final String DEFAULT_SCRIPT_TABLE = "solomon_sql_script_record";

  private final NamedParameterJdbcTemplate jdbcTemplate;

  private final SolomonDataSourceProperties properties;

  private final DataSourceTenantContext tenantContext;

  /**
   * 构造SQL脚本执行工具。
   *
   * @param jdbcTemplate Spring命名参数JDBC模板，脚本执行会跟随动态数据源路由到当前租户
   * @param properties 数据源模块配置，用于读取默认租户、租户数据库类型和脚本记录表配置
   * @param tenantContext 数据源租户上下文，用于读取或切换当前租户编码
   */
  public SqlScriptExecutor(
      NamedParameterJdbcTemplate jdbcTemplate,
      SolomonDataSourceProperties properties,
      DataSourceTenantContext tenantContext) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
    this.tenantContext = tenantContext;
  }

  /**
   * 执行SQL脚本文本。
   *
   * @param scriptCode 脚本编码，作为防重复执行的唯一键，例如 {@code V20260522_001_init_user}
   * @param scriptText SQL脚本文本，支持用分号分隔多条语句，SQL Server脚本也支持单独一行的 {@code GO}
   * @return 脚本执行结果，包含是否跳过、执行语句数和脚本校验值
   * @throws DataSourceException 脚本为空、记录表初始化失败、脚本重复但内容变化或执行失败时抛出
   */
  public SqlScriptExecuteResult execute(String scriptCode, String scriptText)
      throws DataSourceException {
    return execute(scriptCode, scriptText, scriptCode, null);
  }

  /**
   * 执行SQL脚本文本。
   *
   * @param scriptCode 脚本编码，作为防重复执行的唯一键
   * @param scriptText SQL脚本文本，支持多语句
   * @param scriptName 脚本名称，用于写入脚本记录表，便于排查执行历史
   * @param description 脚本说明，用于写入脚本记录表，允许为空
   * @return 脚本执行结果
   * @throws DataSourceException 脚本为空、记录表初始化失败、脚本重复但内容变化或执行失败时抛出
   */
  public SqlScriptExecuteResult execute(
      String scriptCode,
      String scriptText,
      String scriptName,
      String description) throws DataSourceException {
    String fileName = requireText(
        StringUtils.hasText(scriptName) ? scriptName : scriptCode,
        "fileName");
    String normalizedScriptText = requireText(scriptText, "scriptText");
    String fileMd5 = md5(normalizedScriptText.getBytes(StandardCharsets.UTF_8));
    return executeInternal(fileName, normalizedScriptText, fileMd5);
  }

  private SqlScriptExecuteResult executeInternal(
      String fileName,
      String normalizedScriptText,
      String fileMd5) throws DataSourceException {
    long startTime = System.currentTimeMillis();
    ensureScriptTable();
    ScriptRecord record = findRecordByMd5(fileMd5);
    if (record != null && record.success()) {
      return new SqlScriptExecuteResult(fileName, fileMd5, true, true, 0,
          System.currentTimeMillis() - startTime);
    }

    List<String> statements = splitStatements(normalizedScriptText);
    if (statements.isEmpty()) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SCRIPT_EMPTY,
          fileName);
    }

    try {
      JdbcOperations jdbcOperations = jdbcTemplate.getJdbcOperations();
      for (String statement : statements) {
        jdbcOperations.execute(statement);
      }
      long executionTimeMillis = System.currentTimeMillis() - startTime;
      saveScriptRecord(record, fileName, fileMd5, true, null, statements.size(),
          executionTimeMillis);
      return new SqlScriptExecuteResult(fileName, fileMd5, false, true,
          statements.size(), executionTimeMillis);
    } catch (Exception e) {
      saveScriptRecord(record, fileName, fileMd5, false, truncate(e.getMessage(), 2000),
          statements.size(), System.currentTimeMillis() - startTime);
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SCRIPT_EXECUTE_FAILED, e,
          fileName);
    }
  }

  /**
   * 在指定租户数据源中执行SQL脚本。
   *
   * @param tenantCode 租户编码，会先切换到该租户的数据源再执行脚本
   * @param scriptCode 脚本编码，作为防重复执行的唯一键
   * @param scriptText SQL脚本文本
   * @return 脚本执行结果
   * @throws DataSourceException 租户不存在、脚本为空或执行失败时抛出
   */
  public SqlScriptExecuteResult executeForTenant(
      String tenantCode,
      String scriptCode,
      String scriptText) throws DataSourceException {
    return executeForTenant(tenantCode, scriptCode, scriptText, scriptCode, null);
  }

  /**
   * 在指定租户数据源中执行SQL脚本。
   *
   * @param tenantCode 租户编码，会先切换到该租户的数据源再执行脚本
   * @param scriptCode 脚本编码，作为防重复执行的唯一键
   * @param scriptText SQL脚本文本
   * @param scriptName 脚本名称，用于写入脚本记录表
   * @param description 脚本说明，用于写入脚本记录表，允许为空
   * @return 脚本执行结果
   * @throws DataSourceException 租户不存在、脚本为空或执行失败时抛出
   */
  public SqlScriptExecuteResult executeForTenant(
      String tenantCode,
      String scriptCode,
      String scriptText,
      String scriptName,
      String description) throws DataSourceException {
    String normalizedTenantCode = requireText(tenantCode, "tenantCode");
    tenantContext.switchTenant(normalizedTenantCode);
    try {
      return execute(scriptCode, scriptText, scriptName, description);
    } finally {
      tenantContext.removeFactory();
    }
  }

  /**
   * 执行Spring资源中的SQL脚本。
   *
   * @param resource SQL脚本资源，例如 {@code classpath:db/init.sql}
   * @return 脚本执行结果，脚本编码默认使用资源文件名
   * @throws DataSourceException 资源不可读、脚本为空或执行失败时抛出
   */
  public SqlScriptExecuteResult executeResource(Resource resource) throws DataSourceException {
    String filename = resource == null ? null : resource.getFilename();
    return executeResource(filename, resource, filename, null);
  }

  /**
   * 执行Spring资源中的SQL脚本。
   *
   * @param scriptCode 脚本编码，作为防重复执行的唯一键
   * @param resource SQL脚本资源，例如 {@code classpath:db/init.sql}
   * @param scriptName 脚本名称，用于写入脚本记录表
   * @param description 脚本说明，用于写入脚本记录表，允许为空
   * @return 脚本执行结果
   * @throws DataSourceException 资源不可读、脚本为空或执行失败时抛出
   */
  public SqlScriptExecuteResult executeResource(
      String scriptCode,
      Resource resource,
      String scriptName,
      String description) throws DataSourceException {
    if (resource == null) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SCRIPT_EMPTY, "resource");
    }
    try {
      byte[] content = resource.getContentAsByteArray();
      String fileName = requireText(
          StringUtils.hasText(scriptName) ? scriptName : scriptCode,
          "fileName");
      String scriptText = requireText(new String(content, StandardCharsets.UTF_8), "scriptText");
      return executeInternal(fileName, scriptText, md5(content));
    } catch (DataSourceException e) {
      throw e;
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SCRIPT_EXECUTE_FAILED, e,
          scriptCode);
    }
  }

  /**
   * 批量执行Spring资源中的SQL脚本。
   *
   * @param resources SQL脚本资源集合，执行顺序按集合迭代顺序
   * @return 每个脚本对应的执行结果
   * @throws DataSourceException 任意脚本为空或执行失败时抛出
   */
  public List<SqlScriptExecuteResult> executeResources(Collection<Resource> resources)
      throws DataSourceException {
    if (CollectionUtils.isEmpty(resources)) {
      return List.of();
    }
    List<Resource> sortedResources = new ArrayList<>(resources);
    sortedResources.sort(Comparator.comparing(this::resourceSortName));
    List<SqlScriptExecuteResult> results = new ArrayList<>();
    for (Resource resource : sortedResources) {
      results.add(executeResource(resource));
    }
    return results;
  }

  private void ensureScriptTable() throws DataSourceException {
    String tableName = scriptTableName();
    SqlInjectionGuard.validateTableExpression(tableName, "scriptRecordTable");
    try {
      if (!tableExists(tableName)) {
        jdbcTemplate.getJdbcOperations().execute(createTableSql(tableName));
        for (String commentSql : tableCommentSqlList(tableName)) {
          jdbcTemplate.getJdbcOperations().execute(commentSql);
        }
      }
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SCRIPT_TABLE_INIT_FAILED, e,
          tableName);
    }
  }

  private boolean tableExists(String tableName) {
    return Boolean.TRUE.equals(jdbcTemplate.getJdbcOperations().execute(
        (ConnectionCallback<Boolean>) connection -> {
          DatabaseMetaData metaData = connection.getMetaData();
          TableNameParts tableNameParts = parseTableNameParts(tableName, null);
          return tableExists(metaData, connection.getCatalog(), tableNameParts.schema(),
              tableNameParts.table())
              || tableExists(metaData, connection.getCatalog(), upper(tableNameParts.schema()),
              upper(tableNameParts.table()))
              || tableExists(metaData, connection.getCatalog(), lower(tableNameParts.schema()),
              lower(tableNameParts.table()));
        }));
  }

  private boolean tableExists(
      DatabaseMetaData metaData,
      String catalog,
      String schema,
      String table) throws java.sql.SQLException {
    try (ResultSet rs = metaData.getTables(catalog, schema, table, null)) {
      return rs.next();
    }
  }

  private String createTableSql(String tableName) {
    DataBaseTypeEnum databaseType = resolveDatabaseType();
    if (databaseType == DataBaseTypeEnum.ORACLE) {
      return "CREATE TABLE " + tableName + " ("
          + "id NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
          + "create_time TIMESTAMP NOT NULL, "
          + "update_time TIMESTAMP NOT NULL, "
          + "file_name VARCHAR2(255) NOT NULL, "
          + "file_md5 VARCHAR2(32) NOT NULL UNIQUE, "
          + "success NUMBER(1) NOT NULL, "
          + "fail_reason CLOB, "
          + "tenant_code VARCHAR2(100), "
          + "statement_count NUMBER(10), "
          + "execution_time_ms NUMBER(19)"
          + ")";
    }
    if (databaseType == DataBaseTypeEnum.SQL_SERVER) {
      return "CREATE TABLE " + tableName + " ("
          + "id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, "
          + "create_time DATETIME2 NOT NULL, "
          + "update_time DATETIME2 NOT NULL, "
          + "file_name VARCHAR(255) NOT NULL, "
          + "file_md5 VARCHAR(32) NOT NULL UNIQUE, "
          + "success BIT NOT NULL, "
          + "fail_reason NVARCHAR(MAX), "
          + "tenant_code VARCHAR(100), "
          + "statement_count INT, "
          + "execution_time_ms BIGINT"
          + ")";
    }
    String idDefinition = databaseType == DataBaseTypeEnum.POSTGRESQL
        ? "BIGSERIAL PRIMARY KEY"
        : "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY";
    String textDefinition = databaseType == DataBaseTypeEnum.POSTGRESQL ? "TEXT" : "TEXT";
    boolean commentInColumn = databaseType == DataBaseTypeEnum.MYSQL
        || databaseType == DataBaseTypeEnum.MARIADB;
    return "CREATE TABLE " + tableName + " ("
        + "id " + idDefinition + columnComment(commentInColumn, "主键") + ", "
        + "create_time TIMESTAMP NOT NULL" + columnComment(commentInColumn, "创建时间") + ", "
        + "update_time TIMESTAMP NOT NULL" + columnComment(commentInColumn, "修改时间") + ", "
        + "file_name VARCHAR(255) NOT NULL" + columnComment(commentInColumn, "SQL文件名") + ", "
        + "file_md5 VARCHAR(32) NOT NULL UNIQUE" + columnComment(commentInColumn, "SQL文件MD5，唯一校验") + ", "
        + "success BOOLEAN NOT NULL" + columnComment(commentInColumn, "是否执行成功") + ", "
        + "fail_reason " + textDefinition + columnComment(commentInColumn, "失败原因") + ", "
        + "tenant_code VARCHAR(100)" + columnComment(commentInColumn, "租户编码") + ", "
        + "statement_count INT" + columnComment(commentInColumn, "SQL语句数量") + ", "
        + "execution_time_ms BIGINT" + columnComment(commentInColumn, "执行耗时，单位毫秒")
        + ")";
  }

  private String columnComment(boolean enabled, String comment) {
    return enabled ? " COMMENT '" + comment + "'" : "";
  }

  private List<String> tableCommentSqlList(String tableName) {
    DataBaseTypeEnum databaseType = resolveDatabaseType();
    if (databaseType == DataBaseTypeEnum.MYSQL || databaseType == DataBaseTypeEnum.MARIADB) {
      return List.of();
    }
    Map<String, String> comments = scriptRecordColumnComments();
    List<String> sqlList = new ArrayList<>();
    if (databaseType == DataBaseTypeEnum.POSTGRESQL || databaseType == DataBaseTypeEnum.ORACLE) {
      for (Map.Entry<String, String> entry : comments.entrySet()) {
        sqlList.add("COMMENT ON COLUMN " + tableName + "." + entry.getKey() + " IS '"
            + escapeSqlLiteral(entry.getValue()) + "'");
      }
      return sqlList;
    }
    if (databaseType == DataBaseTypeEnum.SQL_SERVER) {
      TableNameParts tableNameParts = parseTableNameParts(tableName, "dbo");
      for (Map.Entry<String, String> entry : comments.entrySet()) {
        sqlList.add("EXEC sp_addextendedproperty "
            + "@name=N'MS_Description', "
            + "@value=N'" + escapeSqlLiteral(entry.getValue()) + "', "
            + "@level0type=N'SCHEMA', @level0name=N'" + escapeSqlLiteral(tableNameParts.schema()) + "', "
            + "@level1type=N'TABLE', @level1name=N'" + escapeSqlLiteral(tableNameParts.table()) + "', "
            + "@level2type=N'COLUMN', @level2name=N'" + escapeSqlLiteral(entry.getKey()) + "'");
      }
    }
    return sqlList;
  }

  private TableNameParts parseTableNameParts(String tableName, String defaultSchema) {
    if (tableName.contains(".")) {
      int index = tableName.lastIndexOf('.');
      return new TableNameParts(tableName.substring(0, index), tableName.substring(index + 1));
    }
    return new TableNameParts(defaultSchema, tableName);
  }

  private String upper(String value) {
    return value == null ? null : value.toUpperCase(Locale.ROOT);
  }

  private String lower(String value) {
    return value == null ? null : value.toLowerCase(Locale.ROOT);
  }

  private String escapeSqlLiteral(String value) {
    return value == null ? "" : value.replace("'", "''");
  }

  private Map<String, String> scriptRecordColumnComments() {
    Map<String, String> comments = new LinkedHashMap<>();
    comments.put("id", "主键");
    comments.put("create_time", "创建时间");
    comments.put("update_time", "修改时间");
    comments.put("file_name", "SQL文件名");
    comments.put("file_md5", "SQL文件MD5，唯一校验");
    comments.put("success", "是否执行成功");
    comments.put("fail_reason", "失败原因");
    comments.put("tenant_code", "租户编码");
    comments.put("statement_count", "SQL语句数量");
    comments.put("execution_time_ms", "执行耗时，单位毫秒");
    return comments;
  }

  private ScriptRecord findRecordByMd5(String fileMd5) {
    try {
      return jdbcTemplate.queryForObject(
          "SELECT id, success FROM " + scriptTableName()
              + " WHERE file_md5 = :fileMd5",
          Map.of("fileMd5", fileMd5),
          (rs, rowNum) -> new ScriptRecord(
              rs.getLong("id"),
              parseSuccess(rs.getObject("success"))));
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  private boolean parseSuccess(Object value) {
    if (value instanceof Boolean booleanValue) {
      return booleanValue;
    }
    if (value instanceof Number numberValue) {
      return numberValue.intValue() == 1;
    }
    return "true".equalsIgnoreCase(String.valueOf(value))
        || "1".equals(String.valueOf(value));
  }

  private void saveScriptRecord(
      ScriptRecord record,
      String fileName,
      String fileMd5,
      boolean success,
      String failReason,
      int statementCount,
      long executionTimeMillis) {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("id", record == null ? null : record.id());
    params.put("fileName", fileName);
    params.put("fileMd5", fileMd5);
    params.put("success", success);
    params.put("failReason", failReason);
    params.put("tenantCode", resolveTenantCode());
    params.put("statementCount", statementCount);
    params.put("executionTimeMillis", executionTimeMillis);
    params.put("now", Timestamp.valueOf(LocalDateTime.now()));
    if (record == null) {
      jdbcTemplate.update("INSERT INTO " + scriptTableName()
              + " (create_time, update_time, file_name, file_md5, success, fail_reason, "
              + "tenant_code, statement_count, execution_time_ms) "
              + "VALUES (:now, :now, :fileName, :fileMd5, :success, :failReason, "
              + ":tenantCode, :statementCount, :executionTimeMillis)",
          params);
      return;
    }
    jdbcTemplate.update("UPDATE " + scriptTableName()
            + " SET update_time = :now, file_name = :fileName, success = :success, "
            + "fail_reason = :failReason, tenant_code = :tenantCode, "
            + "statement_count = :statementCount, execution_time_ms = :executionTimeMillis "
            + "WHERE id = :id",
        params);
  }

  private String scriptTableName() {
    SolomonDataSourceProperties.Script script = properties.getScript();
    if (script != null && StringUtils.hasText(script.getRecordTable())) {
      return script.getRecordTable();
    }
    return DEFAULT_SCRIPT_TABLE;
  }

  private DataBaseTypeEnum resolveDatabaseType() {
    SingleDataSourceProperties tenantProperties = resolveTenantProperties();
    return tenantProperties == null ? DataBaseTypeEnum.MYSQL : tenantProperties.getDatabaseType();
  }

  private SingleDataSourceProperties resolveTenantProperties() {
    String tenantCode = resolveTenantCode();
    SingleDataSourceProperties tenantProperties = properties.getTenants().get(tenantCode);
    if (tenantProperties == null && !properties.getTenants().isEmpty()) {
      tenantProperties = properties.getTenants().values().iterator().next();
    }
    return tenantProperties;
  }

  private String resolveTenantCode() {
    String tenantCode = tenantContext.getCurrentTenantCode();
    if (!StringUtils.hasText(tenantCode)) {
      tenantCode = properties.getDefaultTenant();
    }
    return StringUtils.hasText(tenantCode) ? tenantCode : "default";
  }

  private String requireText(String value, String name) throws DataSourceException {
    if (!StringUtils.hasText(value)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SCRIPT_EMPTY, name);
    }
    return value.trim();
  }

  private String md5(byte[] content) throws DataSourceException {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      byte[] bytes = digest.digest(content);
      StringBuilder builder = new StringBuilder(bytes.length * 2);
      for (byte value : bytes) {
        builder.append(String.format("%02x", value));
      }
      return builder.toString();
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SCRIPT_EXECUTE_FAILED, e,
          "checksum");
    }
  }

  private String resourceSortName(Resource resource) {
    if (resource == null || !StringUtils.hasText(resource.getFilename())) {
      return "";
    }
    return resource.getFilename();
  }

  private String truncate(String value, int maxLength) {
    if (value == null || value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }

  private record ScriptRecord(long id, boolean success) {
  }

  private record TableNameParts(String schema, String table) {
  }

  private List<String> splitStatements(String scriptText) {
    String text = normalizeGoDelimiter(scriptText.replace("\uFEFF", ""));
    List<String> statements = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean singleQuote = false;
    boolean doubleQuote = false;
    boolean lineComment = false;
    boolean blockComment = false;
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';
      if (lineComment) {
        current.append(ch);
        if (ch == '\n') {
          lineComment = false;
        }
        continue;
      }
      if (blockComment) {
        current.append(ch);
        if (ch == '*' && next == '/') {
          current.append(next);
          i++;
          blockComment = false;
        }
        continue;
      }
      if (!singleQuote && !doubleQuote && ch == '-' && next == '-') {
        current.append(ch).append(next);
        i++;
        lineComment = true;
        continue;
      }
      if (!singleQuote && !doubleQuote && ch == '/' && next == '*') {
        current.append(ch).append(next);
        i++;
        blockComment = true;
        continue;
      }
      if (!doubleQuote && ch == '\'') {
        current.append(ch);
        if (singleQuote && next == '\'') {
          current.append(next);
          i++;
        } else {
          singleQuote = !singleQuote;
        }
        continue;
      }
      if (!singleQuote && ch == '"') {
        doubleQuote = !doubleQuote;
        current.append(ch);
        continue;
      }
      if (!singleQuote && !doubleQuote && ch == ';') {
        addStatement(statements, current);
        current.setLength(0);
        continue;
      }
      current.append(ch);
    }
    addStatement(statements, current);
    return statements;
  }

  private String normalizeGoDelimiter(String scriptText) {
    String[] lines = scriptText.split("\\R", -1);
    StringBuilder builder = new StringBuilder(scriptText.length());
    for (String line : lines) {
      if ("GO".equalsIgnoreCase(line.trim())) {
        builder.append(';').append('\n');
      } else {
        builder.append(line).append('\n');
      }
    }
    return builder.toString();
  }

  private void addStatement(List<String> statements, StringBuilder builder) {
    String statement = builder.toString().trim();
    if (StringUtils.hasText(statement)) {
      statements.add(statement);
    }
  }
}
