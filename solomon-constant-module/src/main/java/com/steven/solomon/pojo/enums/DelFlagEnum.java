package com.steven.solomon.pojo.enums;

/**
 * 删除枚举
 */

public enum DelFlagEnum implements BaseEnum<String> {
  /**
   * 未删除
   */
  NOT_DELETE("0","未删除"),
  /**
   * 已删除
   */
  DELETE("1","已删除");

  private String label;

  private String desc;

  DelFlagEnum(String label,String desc) {
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
