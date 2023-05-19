package com.steven.solomon.pojo.enums;

public enum SwitchModeEnum implements BaseEnum<String>{

  NORMAL("单库"), SWITCH_DB("切换数据源"), TENANT_PREFIX("增加租户前缀");

  private String desc;

  SwitchModeEnum(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }

  @Override
  public String label() {
    return this.desc;
  }

  @Override
  public String key() {
    return this.name();
  }
}
