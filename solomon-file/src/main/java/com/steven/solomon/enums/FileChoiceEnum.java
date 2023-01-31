package com.steven.solomon.enums;

import com.steven.solomon.enums.BaseEnum;

public enum FileChoiceEnum implements BaseEnum {
  MINIO("MINIO","minio对象存储"),
  DEFAULT("DEFAULT","无文件存储实现"),
  OSS("OSS","阿里云对象存储"),
  OBS("OBS","华为与对象存储"),
  COS("COS","腾讯云对象存储"),
  BOS("BOS","百度云对象存储"),
  ;

  private final String label;

  private final String description;

  FileChoiceEnum(String label,String description) {
    this.label = label;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String label() {
    return this.label;
  }

  @Override
  public String key() {
    return this.name();
  }

  public String getLabel() {
    return label;
  }
}
