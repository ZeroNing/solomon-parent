package com.steven.solomon.properties;

import com.steven.solomon.enums.FileChoiceEnum;
import com.steven.solomon.enums.FileNamingMethodEnum;
import com.tencentcloudapi.common.profile.Region;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "file")
public class FileChoiceProperties {

  /**
   * 文件服务选择
   */
  private FileChoiceEnum choice = FileChoiceEnum.DEFAULT;
  /**
   * 文件名规则
   */
  private FileNamingMethodEnum fileNamingMethod = FileNamingMethodEnum.ORIGINAL;

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

  /**
   * 地区（默认腾讯的广州）
   */
  private String regionName = Region.Guangzhou.getValue();

  /**
   * 分片大小(默认单位为:MB,默认为5MB)
   */
  private Integer partSize = 5;

  public Integer getPartSize() {
    return partSize;
  }

  public void setPartSize(Integer partSize) {
    this.partSize = partSize;
  }

  public String getRegionName() {
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public FileChoiceEnum getChoice() {
    return choice;
  }

  public void setChoice(FileChoiceEnum choice) {
    this.choice = choice;
  }

  public FileNamingMethodEnum getFileNamingMethod() {
    return fileNamingMethod;
  }

  public void setFileNamingMethod(FileNamingMethodEnum fileNamingMethod) {
    this.fileNamingMethod = fileNamingMethod;
  }

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
