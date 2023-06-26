package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

public enum FileChoiceEnum implements BaseEnum<String> {
  MINIO("MINIO","minio对象存储"),
  DEFAULT("DEFAULT","无文件存储实现"),
  OSS("OSS","阿里云对象存储"),
  OBS("OBS","华为与对象存储"),
  COS("COS","腾讯云对象存储"),
  BOS("BOS","百度云对象存储"),
  KODO("KODO","七牛云对象存储"),
  ;

  private String label;

  private String desc;

  FileChoiceEnum(String label,String desc) {
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
