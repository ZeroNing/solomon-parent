package com.steven.solomon.properties;

import com.steven.solomon.entity.BaseFileProperties;
import com.steven.solomon.enums.FileChoiceEnum;
import com.steven.solomon.enums.FileNamingMethodEnum;
import com.tencentcloudapi.common.profile.Region;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
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
   * 腾讯云存储
   */
  private COSProperties cos;

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

  public COSProperties getCos() {
    return cos;
  }

  public void setCos(COSProperties cos) {
    this.cos = cos;
  }

  //  private MinioProperties minio;
//  /**
//   * 阿里云存储
//   */
//  private OSSProperties oss;
//  /**
//   * 华为云存储
//   */
//  private OBSProperties obs;

//  /**
//   * 百度云存储
//   */
//  private BOSProperties bos;


//  public static class MinioProperties extends BaseFileProperties {
//  }
//
//  public static class OSSProperties extends BaseFileProperties {
//  }
//
//  public static class OBSProperties extends BaseFileProperties {
//  }
//
//  public static class BOSProperties extends BaseFileProperties {
//  }

  public static class COSProperties extends BaseFileProperties {

    private String regionName = Region.Guangzhou.getValue();

    public String getRegionName() {
      return regionName;
    }

    public void setRegionName(String regionName) {
      this.regionName = regionName;
    }
  }
}
