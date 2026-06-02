package com.steven.solomon.datasource.properties;

/**
 * 单租户 Druid SQL 防火墙配置。
 */
public class DruidSecurityProperties {

  private boolean enabled;
  private boolean multiStatementAllow;
  private boolean noneBaseStatementAllow;
  private boolean strictSyntaxCheck = true;
  private boolean commentAllow;
  private boolean conditionAndAlwayTrueAllow;
  private boolean conditionAndAlwayFalseAllow;

  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean value) { this.enabled = value; }
  public boolean isMultiStatementAllow() { return multiStatementAllow; }
  public void setMultiStatementAllow(boolean value) { this.multiStatementAllow = value; }
  public boolean isNoneBaseStatementAllow() { return noneBaseStatementAllow; }
  public void setNoneBaseStatementAllow(boolean value) { this.noneBaseStatementAllow = value; }
  public boolean isStrictSyntaxCheck() { return strictSyntaxCheck; }
  public void setStrictSyntaxCheck(boolean value) { this.strictSyntaxCheck = value; }
  public boolean isCommentAllow() { return commentAllow; }
  public void setCommentAllow(boolean value) { this.commentAllow = value; }
  public boolean isConditionAndAlwayTrueAllow() { return conditionAndAlwayTrueAllow; }
  public void setConditionAndAlwayTrueAllow(boolean value) { this.conditionAndAlwayTrueAllow = value; }
  public boolean isConditionAndAlwayFalseAllow() { return conditionAndAlwayFalseAllow; }
  public void setConditionAndAlwayFalseAllow(boolean value) { this.conditionAndAlwayFalseAllow = value; }
}
