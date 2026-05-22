package com.steven.solomon.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 接口安全防护配置。
 *
 * <p>配置前缀: {@code solomon.security.protection}。</p>
 */
@ConfigurationProperties(prefix = "solomon.security.protection")
public class SecurityProtectionProperties {

  /**
   * 总开关。默认关闭，避免 starter 引入后改变已有接口行为。
   */
  private boolean enabled = false;

  /**
   * 存储类型。
   *
   * <p>memory: 使用本机内存，适合单节点或本地开发。
   * redis: 使用 solomon-redis 提供的 Redis 存储，适合多节点部署。
   * custom: 业务方自行提供 ProtectionStore Bean。</p>
   */
  private String storeType = "memory";

  /**
   * 租户 ID 请求头。
   */
  private String tenantIdHeader = "X-Tenant-Id";

  /**
   * 租户编码请求头。安全 key 优先使用 tenantCode。
   */
  private String tenantCodeHeader = "X-Tenant-Code";

  /**
   * 租户名称请求头，仅用于上下文传递。
   */
  private String tenantNameHeader = "X-Tenant-Name";

  /**
   * 全局排除路径，例如 /actuator/**、/doc.html。
   */
  private List<String> excludePaths = new ArrayList<>();

  private DuplicateSubmit duplicateSubmit = new DuplicateSubmit();

  private Signature signature = new Signature();

  private RateLimit rateLimit = new RateLimit();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getStoreType() {
    return storeType;
  }

  public void setStoreType(String storeType) {
    this.storeType = storeType;
  }

  public String getTenantIdHeader() {
    return tenantIdHeader;
  }

  public void setTenantIdHeader(String tenantIdHeader) {
    this.tenantIdHeader = tenantIdHeader;
  }

  public String getTenantCodeHeader() {
    return tenantCodeHeader;
  }

  public void setTenantCodeHeader(String tenantCodeHeader) {
    this.tenantCodeHeader = tenantCodeHeader;
  }

  public String getTenantNameHeader() {
    return tenantNameHeader;
  }

  public void setTenantNameHeader(String tenantNameHeader) {
    this.tenantNameHeader = tenantNameHeader;
  }

  public List<String> getExcludePaths() {
    return excludePaths;
  }

  public void setExcludePaths(List<String> excludePaths) {
    this.excludePaths = excludePaths;
  }

  public DuplicateSubmit getDuplicateSubmit() {
    return duplicateSubmit;
  }

  public void setDuplicateSubmit(DuplicateSubmit duplicateSubmit) {
    this.duplicateSubmit = duplicateSubmit;
  }

  public Signature getSignature() {
    return signature;
  }

  public void setSignature(Signature signature) {
    this.signature = signature;
  }

  public RateLimit getRateLimit() {
    return rateLimit;
  }

  public void setRateLimit(RateLimit rateLimit) {
    this.rateLimit = rateLimit;
  }

  public static class DuplicateSubmit {

    /**
     * 是否启用防重复提交。
     */
    private boolean enabled = false;

    /**
     * 需要启用防重复提交的路径。为空表示所有路径。
     */
    private List<String> paths = new ArrayList<>();

    /**
     * 幂等请求头。前端可以为每次提交生成一个唯一值。
     */
    private String tokenHeader = "X-Idempotency-Key";

    /**
     * 幂等 key 保存时间。
     */
    private long expireSeconds = 10;

    /**
     * 未传幂等请求头时，是否把 body hash 纳入请求指纹。
     */
    private boolean includeBodyHash = true;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public List<String> getPaths() {
      return paths;
    }

    public void setPaths(List<String> paths) {
      this.paths = paths;
    }

    public String getTokenHeader() {
      return tokenHeader;
    }

    public void setTokenHeader(String tokenHeader) {
      this.tokenHeader = tokenHeader;
    }

    public long getExpireSeconds() {
      return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
      this.expireSeconds = expireSeconds;
    }

    public boolean isIncludeBodyHash() {
      return includeBodyHash;
    }

    public void setIncludeBodyHash(boolean includeBodyHash) {
      this.includeBodyHash = includeBodyHash;
    }
  }

  public static class Signature {

    /**
     * 是否启用签名防篡改。
     */
    private boolean enabled = false;

    /**
     * 需要验签的路径。为空表示所有路径。
     */
    private List<String> paths = new ArrayList<>();

    private String appKeyHeader = "X-App-Key";

    private String timestampHeader = "X-Timestamp";

    private String nonceHeader = "X-Nonce";

    private String signHeader = "X-Sign";

    /**
     * 默认签名密钥。未配置租户密钥时使用。
     */
    private String defaultSecret;

    /**
     * 租户密钥映射。
     *
     * <p>key 可以是 tenantCode，也可以是 appKey。优先 tenantCode，其次 appKey。</p>
     */
    private Map<String, String> tenantSecrets = new HashMap<>();

    /**
     * 时间戳允许偏移秒数。
     */
    private long timestampTtlSeconds = 300;

    /**
     * nonce 防重放有效期秒数。
     */
    private long nonceTtlSeconds = 300;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public List<String> getPaths() {
      return paths;
    }

    public void setPaths(List<String> paths) {
      this.paths = paths;
    }

    public String getAppKeyHeader() {
      return appKeyHeader;
    }

    public void setAppKeyHeader(String appKeyHeader) {
      this.appKeyHeader = appKeyHeader;
    }

    public String getTimestampHeader() {
      return timestampHeader;
    }

    public void setTimestampHeader(String timestampHeader) {
      this.timestampHeader = timestampHeader;
    }

    public String getNonceHeader() {
      return nonceHeader;
    }

    public void setNonceHeader(String nonceHeader) {
      this.nonceHeader = nonceHeader;
    }

    public String getSignHeader() {
      return signHeader;
    }

    public void setSignHeader(String signHeader) {
      this.signHeader = signHeader;
    }

    public String getDefaultSecret() {
      return defaultSecret;
    }

    public void setDefaultSecret(String defaultSecret) {
      this.defaultSecret = defaultSecret;
    }

    public Map<String, String> getTenantSecrets() {
      return tenantSecrets;
    }

    public void setTenantSecrets(Map<String, String> tenantSecrets) {
      this.tenantSecrets = tenantSecrets;
    }

    public long getTimestampTtlSeconds() {
      return timestampTtlSeconds;
    }

    public void setTimestampTtlSeconds(long timestampTtlSeconds) {
      this.timestampTtlSeconds = timestampTtlSeconds;
    }

    public long getNonceTtlSeconds() {
      return nonceTtlSeconds;
    }

    public void setNonceTtlSeconds(long nonceTtlSeconds) {
      this.nonceTtlSeconds = nonceTtlSeconds;
    }
  }

  public static class RateLimit {

    /**
     * 是否启用防刷限流。
     */
    private boolean enabled = false;

    /**
     * 需要限流的路径。为空表示所有路径。
     */
    private List<String> paths = new ArrayList<>();

    /**
     * 一个窗口内允许的请求数。
     */
    private int permits = 60;

    /**
     * 固定窗口大小，单位秒。
     */
    private long windowSeconds = 60;

    /**
     * 是否把客户端 IP 纳入限流 key。
     */
    private boolean includeClientIp = true;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public List<String> getPaths() {
      return paths;
    }

    public void setPaths(List<String> paths) {
      this.paths = paths;
    }

    public int getPermits() {
      return permits;
    }

    public void setPermits(int permits) {
      this.permits = permits;
    }

    public long getWindowSeconds() {
      return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
      this.windowSeconds = windowSeconds;
    }

    public boolean isIncludeClientIp() {
      return includeClientIp;
    }

    public void setIncludeClientIp(boolean includeClientIp) {
      this.includeClientIp = includeClientIp;
    }
  }
}
