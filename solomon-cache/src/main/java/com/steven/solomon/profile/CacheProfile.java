package com.steven.solomon.profile;

import com.steven.solomon.enums.CacheModeEnum;
import com.steven.solomon.enums.CacheTypeEnum;
import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("cache")
@Component
public class CacheProfile {

  /**
   * redis缓存模式（默认单库）
   */
  private String mode = CacheModeEnum.NORMAL.toString();

  /**
   * 缓存类型
   */
  private String type = CacheTypeEnum.REDIS.toString();

  /**
   * redis配置环境
   */
  private RedisProfile redisProfile;

  public static class RedisProfile implements Serializable {

    /**
     * Redis连接URl
     */
    private String url;

    /**
     * redis主机名
     */
    private String host = "localhost";
    /**
     * 端口
     */
    private int port = 6379;
    /**
     * 密码
     */
    private String password;

    /**
     * 数据库
     */
    private int database = 0;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public int getDatabase() {
      return database;
    }

    public void setDatabase(int database) {
      this.database = database;
    }

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public RedisProfile getRedisProfile() {
    return redisProfile;
  }

  public void setRedisProfile(RedisProfile redisProfile) {
    this.redisProfile = redisProfile;
  }
}
