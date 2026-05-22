package com.steven.solomon.datasource.sql.script;

/**
 * SQL脚本执行结果。
 */
public class SqlScriptExecuteResult {

  /** 脚本编码，作为脚本记录表的唯一标识。 */
  private final String scriptCode;

  /** 脚本内容SHA-256校验值，用于发现同一编码脚本内容被修改的情况。 */
  private final String checksum;

  /** 是否因为脚本已经执行过而跳过。 */
  private final boolean skipped;

  /** 本次实际执行的SQL语句数量。 */
  private final int statementCount;

  /** 本次执行耗时，单位毫秒。 */
  private final long executionTimeMillis;

  /**
   * 构造脚本执行结果。
   *
   * @param scriptCode 脚本编码，不能为空
   * @param checksum 脚本内容SHA-256校验值
   * @param skipped 是否跳过执行
   * @param statementCount 实际执行的SQL语句数量
   * @param executionTimeMillis 执行耗时，单位毫秒
   */
  public SqlScriptExecuteResult(
      String scriptCode,
      String checksum,
      boolean skipped,
      int statementCount,
      long executionTimeMillis) {
    this.scriptCode = scriptCode;
    this.checksum = checksum;
    this.skipped = skipped;
    this.statementCount = statementCount;
    this.executionTimeMillis = executionTimeMillis;
  }

  public String getScriptCode() {
    return scriptCode;
  }

  public String getChecksum() {
    return checksum;
  }

  public boolean isSkipped() {
    return skipped;
  }

  public int getStatementCount() {
    return statementCount;
  }

  public long getExecutionTimeMillis() {
    return executionTimeMillis;
  }
}
