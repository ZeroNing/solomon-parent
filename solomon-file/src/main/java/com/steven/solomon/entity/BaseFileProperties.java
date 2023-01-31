package com.steven.solomon.entity;

import java.io.Serializable;

public class BaseFileProperties implements Serializable {

  /**
   * 是一个URL，域名，IPv4或者IPv6地址")
   */
  private String endpoint;

  /**
   * "accessKey类似于用户ID，用于唯一标识你的账户"
   */
  private String accessKey;

  /**
   * "secretKey是你账户的密码"
   */
  private String secretKey;

  /**
   * "默认存储桶"
   */
  private String bucketName;

  /**
   * 根目录
   */
  private String rootDirectory;

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getRootDirectory() {
    return rootDirectory;
  }

  public void setRootDirectory(String rootDirectory) {
    this.rootDirectory = rootDirectory;
  }
}
