package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

public enum FileNamingMethodEnum implements BaseEnum<String> {
  ORIGINAL("ORIGINAL","使用文件的文件名"),
  DATE("DATE","根据时间戳生成文件名"),
  UUID("UUID","根据UUID生成文件名"),
  SNOWFLAKE("SNOWFLAKE","根据雪花id生成文件名");

  private String label;

  private String desc;

  FileNamingMethodEnum(String label,String desc) {
    this.label = label;
    this.desc = desc;
  }

  @Override
  public String getDesc() {
    return desc;
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
