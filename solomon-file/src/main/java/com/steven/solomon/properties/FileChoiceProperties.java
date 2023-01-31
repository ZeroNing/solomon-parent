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

  private MinioProperties minio;
  /**
   * 阿里云存储
   */
  private OSSProperties oss;
  /**
   * 华为云存储
   */
  private OBSProperties obs;
  /**
   * 腾讯云存储
   */
  private COSProperties cos;
  /**
   * 百度云存储
   */
  private BOSProperties bos;

  public BOSProperties getBos() {
    return bos;
  }

  public void setBos(BOSProperties bos) {
    this.bos = bos;
  }

  public COSProperties getCos() {
    return cos;
  }

  public void setCos(COSProperties cos) {
    this.cos = cos;
  }

  public OBSProperties getObs() {
    return obs;
  }

  public void setObs(OBSProperties obs) {
    this.obs = obs;
  }

  public OSSProperties getOss() {
    return oss;
  }

  public void setOss(OSSProperties oss) {
    this.oss = oss;
  }

  public FileChoiceEnum getChoice() {
    return choice;
  }

  public void setChoice(FileChoiceEnum choice) {
    this.choice = choice;
  }

  public MinioProperties getMinio() {
    return minio;
  }

  public void setMinio(MinioProperties minio) {
    this.minio = minio;
  }

  public FileNamingMethodEnum getFileNamingMethod() {
    return fileNamingMethod;
  }

  public void setFileNamingMethod(FileNamingMethodEnum fileNamingMethod) {
    this.fileNamingMethod = fileNamingMethod;
  }

  public static class MinioProperties extends BaseFileProperties {
  }

  public static class OSSProperties extends BaseFileProperties {
  }

  public static class OBSProperties extends BaseFileProperties {
  }

  public static class BOSProperties extends BaseFileProperties {
  }

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
