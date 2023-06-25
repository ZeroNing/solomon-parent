package com.steven.solomon.pojo.enums;

public enum SwitchModeEnum implements BaseEnum<String>{

  NORMAL("NORMAL","单库"), SWITCH_DB("SWITCH_DB","切换数据源"), TENANT_PREFIX("TENANT_PREFIX","增加租户前缀");

  private String label;

  private String desc;

  SwitchModeEnum(String label,String desc) {
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
