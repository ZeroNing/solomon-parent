package com.steven.solomon.persistence.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 深浅分页切换配置。
 */
public class PaginationProperties {

  private boolean autoSeekEnabled = true;
  @Min(value = 1, message = "persistence.page.seek-page-no must be at least 1")
  private int seekPageNo = 500;
  @Min(value = 1, message = "persistence.page.seek-page-size must be at least 1")
  private int seekPageSize = 10;
  @NotBlank(message = "persistence.page.default-seek-column must not be blank")
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
