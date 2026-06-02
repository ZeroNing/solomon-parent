package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * 文件命名方式枚举。
 *
 * <p>定义上传文件时的文件名生成策略，
 * 用于配置文件中 {@code file.file-naming-method} 属性的可选值。</p>
 */
public enum FileNamingMethodEnum implements BaseEnum<String> {
  ORIGINAL("ORIGINAL","使用文件的文件名"),
  DATE("DATE","根据时间戳生成文件名"),
  UUID("UUID","根据UUID生成文件名"),
  SNOWFLAKE("SNOWFLAKE","根据雪花id生成文件名");

  /** 枚举标签，与配置值对应。 */
  private String label;

  /** 枚举描述，说明命名方式。 */
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
