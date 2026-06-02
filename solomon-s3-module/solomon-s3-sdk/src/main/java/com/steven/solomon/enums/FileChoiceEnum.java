package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * 对象存储供应商枚举。
 *
 * <p>定义所有支持的对象存储服务商，每个枚举项对应一个供应商，
 * 用于配置文件中 {@code file.choice} 属性的可选值。</p>
 */
public enum FileChoiceEnum implements BaseEnum<String> {
  DEFAULT("DEFAULT","无文件存储实现"),
  MINIO("MINIO","minio对象存储"),
  OSS("OSS","阿里云对象存储"),
  OBS("OBS","华为云对象存储"),
  COS("COS","腾讯云对象存储"),
  BOS("BOS","百度云对象存储"),
  KODO("KODO","七牛云对象存储"),
  ZOS("ZOS","天翼云对象存储"),
  KS3("KS3","金山云对象存储"),
  EOS("EOS","移动云对象存储"),
  NOS("NOS","网易数帆对象存储"),
  B2("B2","B2云存储"),
  JD("JD","京东云存储"),
  YANDEX("YANDEX","Yandex对象存储"),
  AMAZON("AMAZON","亚马逊对象存储"),
  SHARKTECH("SHARKTECH","鲨鱼对象存储"),
  DIDI("DIDI","滴滴云对象存储"),
  BOTO3("BOTO3","交大云对象存储"),
  TOS("TOS","火山云对象存储"),
  R2("R2","R2对象存储"),
  GOOGLE_CLOUD_STORAGE("GOOGLE_CLOUD_STORAGE","谷歌云对象存储"),
  UOS("UOS","紫光云对象存储"),
  AZURE("AZURE","微软对象存储"),
  INSPUR("INSPUR","浪潮云对象存储"),
  S3("S3","S3协议对象存储")
  ;

  /** 枚举标签，与配置值对应。 */
  private String label;

  /** 枚举描述，说明供应商名称。 */
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
