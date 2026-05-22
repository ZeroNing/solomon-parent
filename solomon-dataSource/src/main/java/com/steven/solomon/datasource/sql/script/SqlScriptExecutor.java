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
    String normalizedScriptCode = requireText(scriptCode, "scriptCode");
    String normalizedScriptText = requireText(scriptText, "scriptText");
    String checksum = checksum(normalizedScriptText);
    long startTime = System.currentTimeMillis();
    ensureScriptTable();
    String oldChecksum = findExecutedChecksum(normalizedScriptCode);
    if (StringUtils.hasText(oldChecksum)) {
      if (!oldChecksum.equals(checksum)) {
        throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SCRIPT_CHECKSUM_CHANGED,
            normalizedScriptCode);
      }
      return new SqlScriptExecuteResult(normalizedScriptCode, checksum, true, 0,
          System.currentTimeMillis() - startTime);
    }

    List<String> statements = splitStatements(normalizedScriptText);
    if (statements.isEmpty()) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SCRIPT_EMPTY,
          normalizedScriptCode);
    }

    try {
      JdbcOperations jdbcOperations = jdbcTemplate.getJdbcOperations();
      for (String statement : statements) {
        jdbcOperations.execute(statement);
      }
      long executionTimeMillis = System.currentTimeMillis() - startTime;
      insertScriptRecord(normalizedScriptCode, checksum, scriptName, description,
          statements.size(), executionTimeMillis);
      return new SqlScriptExecuteResult(normalizedScriptCode, checksum, false,
          statements.size(), executionTimeMillis);
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SCRIPT_EXECUTE_FAILED, e,
          normalizedScriptCode);
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
      return execute(scriptCode, resource.getContentAsString(StandardCharsets.UTF_8), scriptName,
          description);
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
    List<SqlScriptExecuteResult> results = new ArrayList<>();
    for (Resource resource : resources) {
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
          String upperTableName = tableName.toUpperCase(Locale.ROOT);
          String lowerTableName = tableName.toLowerCase(Locale.ROOT);
          try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, tableName, null)) {
            if (rs.next()) {
              return true;
            }
          }
          try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, upperTableName,
              null)) {
            if (rs.next()) {
              return true;
            }
          }
          try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, lowerTableName,
              null)) {
            return rs.next();
          }
        }));
  }

  private String createTableSql(String tableName) {
    DataBaseTypeEnum databaseType = resolveDatabaseType();
    if (databaseType == DataBaseTypeEnum.ORACLE) {
      return "CREATE TABLE " + tableName + " ("
          + "script_code VARCHAR2(200) NOT NULL PRIMARY KEY, "
          + "script_checksum VARCHAR2(64) NOT NULL, "
          + "script_name VARCHAR2(255), "
          + "script_description CLOB, "
          + "tenant_code VARCHAR2(100), "
          + "statement_count NUMBER(10), "
          + "execution_time_ms NUMBER(19), "
          + "executed_at TIMESTAMP NOT NULL"
          + ")";
    }
    if (databaseType == DataBaseTypeEnum.SQL_SERVER) {
      return "CREATE TABLE " + tableName + " ("
          + "script_code VARCHAR(200) NOT NULL PRIMARY KEY, "
          + "script_checksum VARCHAR(64) NOT NULL, "
          + "script_name VARCHAR(255), "
          + "script_description NVARCHAR(MAX), "
          + "tenant_code VARCHAR(100), "
          + "statement_count INT, "
          + "execution_time_ms BIGINT, "
          + "executed_at DATETIME2 NOT NULL"
          + ")";
    }
    return "CREATE TABLE " + tableName + " ("
        + "script_code VARCHAR(200) NOT NULL PRIMARY KEY, "
        + "script_checksum VARCHAR(64) NOT NULL, "
        + "script_name VARCHAR(255), "
        + "script_description TEXT, "
        + "tenant_code VARCHAR(100), "
        + "statement_count INT, "
        + "execution_time_ms BIGINT, "
        + "executed_at TIMESTAMP NOT NULL"
        + ")";
  }

  private String findExecutedChecksum(String scriptCode) {
    try {
      return jdbcTemplate.queryForObject(
          "SELECT script_checksum FROM " + scriptTableName()
              + " WHERE script_code = :scriptCode",
          Map.of("scriptCode", scriptCode),
          String.class);
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  private void insertScriptRecord(
      String scriptCode,
      String checksum,
      String scriptName,
      String description,
      int statementCount,
      long executionTimeMillis) {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("scriptCode", scriptCode);
    params.put("checksum", checksum);
    params.put("scriptName", StringUtils.hasText(scriptName) ? scriptName : scriptCode);
    params.put("description", description);
    params.put("tenantCode", resolveTenantCode());
    params.put("statementCount", statementCount);
    params.put("executionTimeMillis", executionTimeMillis);
    params.put("executedAt", Timestamp.valueOf(LocalDateTime.now()));
    jdbcTemplate.update("INSERT INTO " + scriptTableName()
            + " (script_code, script_checksum, script_name, script_description, tenant_code, "
            + "statement_count, execution_time_ms, executed_at) "
            + "VALUES (:scriptCode, :checksum, :scriptName, :description, :tenantCode, "
            + ":statementCount, :executionTimeMillis, :executedAt)",
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

  private String checksum(String scriptText) throws DataSourceException {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(scriptText.getBytes(StandardCharsets.UTF_8));
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
