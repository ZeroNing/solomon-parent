package com.steven.solomon.datasource.properties;

/**
 * 深浅分页切换配置。
 */
public class PaginationProperties {

  private boolean autoSeekEnabled = true;
  private int seekPageNo = 500;
  private int seekPageSize = 10;
  private String defaultSeekColumn = "id";

  public boolean isAutoSeekEnabled() { return autoSeekEnabled; }
  public void setAutoSeekEnabled(boolean value) { this.autoSeekEnabled = value; }
  public int getSeekPageNo() { return seekPageNo; }
  public void setSeekPageNo(int value) { this.seekPageNo = value; }
  public int getSeekPageSize() { return seekPageSize; }
  public void setSeekPageSize(int value) { this.seekPageSize = value; }
  public String getDefaultSeekColumn() { return defaultSeekColumn; }
  public void setDefaultSeekColumn(String value) { this.defaultSeekColumn = value; }
}
