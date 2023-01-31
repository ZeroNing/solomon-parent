package com.steven.solomon.enums;

public enum MongoModeEnum {

  NORMAL("单库"), SWITCH_DB("切换数据源");

  private String desc;

  MongoModeEnum(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }
}
