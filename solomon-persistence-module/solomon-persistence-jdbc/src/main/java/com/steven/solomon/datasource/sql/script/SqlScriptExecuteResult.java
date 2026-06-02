package com.steven.solomon.datasource.sql.script;

/**
 * SQL脚本执行结果。
 */
public class SqlScriptExecuteResult {

  /** SQL文件名或脚本名。 */
  private final String fileName;

  /** SQL文件内容的MD5值，用于唯一校验和防重复执行。 */
  private final String fileMd5;

  /** 是否因为脚本已经成功执行过而跳过。 */
  private final boolean skipped;

  /** 本次脚本是否执行成功。 */
  private final boolean success;

  /** 本次实际执行的SQL语句数量。 */
  private final int statementCount;

  /** 本次执行耗时，单位毫秒。 */
  private final long executionTimeMillis;

  /**
   * 构造脚本执行结果。
   *
   * @param fileName SQL文件名或脚本名
   * @param fileMd5 SQL文件内容的MD5值
   * @param skipped 是否因为已经成功执行过而跳过
   * @param success 本次脚本是否执行成功
   * @param statementCount 实际执行的SQL语句数量
   * @param executionTimeMillis 执行耗时，单位毫秒
   */
  public SqlScriptExecuteResult(
      String fileName,
      String fileMd5,
      boolean skipped,
      boolean success,
      int statementCount,
      long executionTimeMillis) {
    this.fileName = fileName;
    this.fileMd5 = fileMd5;
    this.skipped = skipped;
    this.success = success;
    this.statementCount = statementCount;
    this.executionTimeMillis = executionTimeMillis;
  }

  public String getFileName() {
    return fileName;
  }

  public String getFileMd5() {
    return fileMd5;
  }

  public boolean isSkipped() {
    return skipped;
  }

  public boolean isSuccess() {
    return success;
  }

  public int getStatementCount() {
    return statementCount;
  }

  public long getExecutionTimeMillis() {
    return executionTimeMillis;
  }

  /**
   * 兼容旧调用，返回文件名。
   *
   * @return SQL文件名或脚本名
   */
  public String getScriptCode() {
    return fileName;
  }

  /**
   * 兼容旧调用，返回文件MD5。
   *
   * @return SQL文件内容的MD5值
   */
  public String getChecksum() {
    return fileMd5;
  }
}
