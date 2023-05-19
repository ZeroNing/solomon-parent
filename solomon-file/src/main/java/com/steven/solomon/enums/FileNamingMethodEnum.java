package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

public enum FileNamingMethodEnum implements BaseEnum<String> {
  ORIGINAL("ORIGINAL"),
  DATE("DATE"),
  UUID("UUID"),
  SNOWFLAKE("SNOWFLAKE");

  private final String label;

  FileNamingMethodEnum(String label) {
    this.label = label;
  }

  @Override
  public String label() {
    return this.label;
  }

  @Override
  public String key() {
    return this.name();
  }
}
