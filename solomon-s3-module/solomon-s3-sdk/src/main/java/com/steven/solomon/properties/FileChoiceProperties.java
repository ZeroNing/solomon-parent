package com.steven.solomon.properties;

import com.steven.solomon.enums.FileChoiceEnum;
import com.steven.solomon.enums.FileNamingMethodEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置属性。
 *
 * <p>对应配置文件中以 {@code file} 为前缀的配置项，
 * 包括供应商选择、访问凭证、存储桶、超时等参数。</p>
 */
@ConfigurationProperties(prefix = "file")
public class FileChoiceProperties {

  /** 默认地区名称（腾讯云广州）。 */
  private static final String DEFAULT_REGION_NAME = "ap-guangzhou";

  /** 文件服务供应商选择。 */
  private FileChoiceEnum choice = FileChoiceEnum.DEFAULT;
  /** 文件命名规则方式。 */
  private FileNamingMethodEnum fileNamingMethod = FileNamingMethodEnum.ORIGINAL;

  /** 服务端点地址（URL、域名、IPv4 或 IPv6 地址）。 */
  private String endpoint;

  /** 访问密钥 ID，用于唯一标识用户账户。 */
  private String accessKey;

  /** 秘密密钥，用于验证用户身份。 */
  private String secretKey;

  /** 默认存储桶名称。 */
  private String bucketName;

  /** 文件存储根目录。 */
  private String rootDirectory;

  /** 地区（默认使用腾讯云广州）。 */
  private String regionName = DEFAULT_REGION_NAME;

  /**
   * 分片大小，单位为 MB，默认 5MB。
   *
   * <p>用于大文件分片上传时每个分片的大小。</p>
   */
  private Integer partSize = 5;

  /** 连接超时时间，单位毫秒，默认 60 秒。 */
  private Integer connectionTimeout = 60000;

  /** Socket 读取超时时间，单位毫秒，默认 60 秒。 */
  private Integer socketTimeout = 60000;

  /**
   * S3 客户端是否使用路径样式访问（Path-Style），
   * 而不是虚拟主机样式（Virtual-Hosted-Style）。
   */
  private boolean pathStyleAccessEnabled = false;

  /** 启动时是否检查默认桶是否存在。 */
  private boolean checkBucketOnStartup = false;

  /** 默认桶不存在时是否自动创建。 */
  private boolean autoCreateBucket = true;

  public boolean getCheckBucketOnStartup() {
    return checkBucketOnStartup;
  }

  public void setCheckBucketOnStartup(boolean checkBucketOnStartup) {
    this.checkBucketOnStartup = checkBucketOnStartup;
  }

  public boolean getAutoCreateBucket() {
    return autoCreateBucket;
  }

  public void setAutoCreateBucket(boolean autoCreateBucket) {
    this.autoCreateBucket = autoCreateBucket;
  }

  public boolean getPathStyleAccessEnabled() {
    return pathStyleAccessEnabled;
  }

  public void setPathStyleAccessEnabled(boolean pathStyleAccessEnabled) {
    this.pathStyleAccessEnabled = pathStyleAccessEnabled;
  }

  public Integer getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(Integer connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public Integer getSocketTimeout() {
    return socketTimeout;
  }

  public void setSocketTimeout(Integer socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

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
