package com.steven.solomon.enums;

public enum CacheTypeEnum {

  REDIS("redis");

  private String desc;

  CacheTypeEnum(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }
}
