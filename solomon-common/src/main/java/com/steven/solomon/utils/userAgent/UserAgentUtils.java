package com.steven.solomon.utils.userAgent;

import cn.hutool.core.util.ObjectUtil;

import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

/**
 * User-Agent 解析工具。
 *
 * <p>{@link UserAgentAnalyzer} 初始化成本较高，因此作为单例 Bean 复用。
 * 当前只加载常用字段，避免加载全部规则造成不必要的启动和解析开销。</p>
 */
@AutoConfiguration
@ConditionalOnMissingBean(UserAgentUtils.class)
public class UserAgentUtils {

  private static final String USER_AGENT_HEADER = "User-Agent";

  private final UserAgentAnalyzer userAgentAnalyzer;

  public UserAgentUtils() {
    this.userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
        .withFields(
            UserAgent.AGENT_NAME_VERSION,
            UserAgent.OPERATING_SYSTEM_NAME_VERSION,
            UserAgent.DEVICE_BRAND,
            UserAgent.DEVICE_CLASS,
            UserAgent.DEVICE_NAME)
        .hideMatcherLoadStats()
        .withCache(10000)
        .build();
  }

  /**
   * 获取请求中的原始 User-Agent 字符串。
   */
  public String getAgentString(HttpServletRequest request) {
    return ObjectUtil.isEmpty(request) ? null : request.getHeader(USER_AGENT_HEADER);
  }

  /**
   * 解析请求中的 User-Agent。
   */
  public com.steven.solomon.utils.userAgent.entity.UserAgent getUserAgentVO(
      HttpServletRequest request) {
    return getUserAgentVO(getAgentString(request));
  }

  /**
   * 解析原始 User-Agent 字符串。
   *
   * @param agent 原始 User-Agent 字符串
   * @return 结构化 User-Agent 信息
   */
  public com.steven.solomon.utils.userAgent.entity.UserAgent getUserAgentVO(String agent) {
    UserAgent userAgent = userAgentAnalyzer.parse(agent);
    com.steven.solomon.utils.userAgent.entity.UserAgent agentVO =
        new com.steven.solomon.utils.userAgent.entity.UserAgent();
    agentVO.setDevice(userAgent.getValue(UserAgent.DEVICE_NAME));
    agentVO.setDeviceType(userAgent.getValue(UserAgent.DEVICE_CLASS));
    agentVO.setDeviceBrand(userAgent.getValue(UserAgent.DEVICE_BRAND));
    agentVO.setBrowserNameVersion(userAgent.getValue(UserAgent.AGENT_NAME_VERSION));
    agentVO.setOsNameVersion(userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME_VERSION));
    agentVO.setUserAgent(agent);
    return agentVO;
  }
}
