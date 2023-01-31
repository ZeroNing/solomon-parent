package com.steven.solomon.enums;

public enum CacheModeEnum {

  NORMAL("单库"), SWITCH_DB("切换数据源"), TENANT_PREFIX("增加租户前缀");

  private String desc;

  CacheModeEnum(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }
}
