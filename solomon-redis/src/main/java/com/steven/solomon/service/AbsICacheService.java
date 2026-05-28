package com.steven.solomon.service;

import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.profile.CacheProfile;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;

/**
 * 缓存服务抽象基类。
 *
 * <p>提供通用的缓存key组装逻辑，根据缓存模式决定是否拼接租户前缀：</p>
 * <ul>
 *   <li>TENANT_PREFIX 模式：key格式为 {@code 租户编码:group:key}</li>
 *   <li>其他模式：key格式为 {@code group:key}</li>
 * </ul>
 */
public abstract class AbsICacheService implements  ICacheService {

  /** 日志记录器 */
  protected final Logger logger = LoggerUtils.logger(AbsICacheService.class);

  /** 缓存模式配置 */
  @Resource
  private CacheProfile properties;

  /**
   * 组装缓存key。
   *
   * <p>根据当前缓存模式，在key前面拼接租户编码和分组名。</p>
   *
   * @param group 缓存分组名
   * @param key   原始缓存key
   * @return 组装后的完整缓存key
   */
  public String assembleKey(String group, String key) {
    StringBuilder sb = new StringBuilder();
    // TENANT_PREFIX模式下，拼接租户编码前缀
    if (ValidateUtils.isNotEmpty(properties) && ValidateUtils.equalsIgnoreCase(SwitchModeEnum.TENANT_PREFIX.toString(),properties.getMode().toString())) {
      String tenantCode = RequestHeaderHolder.getTenantCode();
      if (ValidateUtils.isEmpty(tenantCode)) {
        logger.error("当前模式是:{},但是缺乏租户信息,所以不拼接",SwitchModeEnum.TENANT_PREFIX.getDesc());
      } else {
        sb.append(tenantCode).append(":");
      }
    }
    // 拼接分组名
    if (ValidateUtils.isNotEmpty(group)) {
      sb.append(group).append(":");
    }
    return sb.append(key).toString();
  }
}
