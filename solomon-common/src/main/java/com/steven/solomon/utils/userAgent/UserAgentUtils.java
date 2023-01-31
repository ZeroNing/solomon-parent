package com.steven.solomon.utils.userAgent;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

public final class UserAgentUtils {

  private static final String USER_AGENT = "User-Agent";
  private UserAgentAnalyzer userAgentAnalyzer = null;

  public UserAgentUtils() {
    userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
        .withFields(UserAgent.AGENT_NAME_VERSION, UserAgent.OPERATING_SYSTEM_NAME_VERSION, UserAgent.DEVICE_BRAND, UserAgent.DEVICE_CLASS, UserAgent.DEVICE_NAME)
        .hideMatcherLoadStats().withCache(10000).build();
  }

  /**
   * 获取agent字符串
   */
  public final String getAgentString(HttpServletRequest request) {
    String agent = request.getHeader(USER_AGENT);
    return agent;
  }

  /**
   * 获取浏览器名称跟版本号
   *
   * @param userAgent
   * @return
   * @throws IOException
   */
  public final String getBrowserNameVersion(UserAgent userAgent) {
    return userAgent.getValue(UserAgent.AGENT_NAME_VERSION);
  }

  /**
   * 获取设备名称
   *
   * @param userAgent
   * @return
   * @throws IOException
   */
  public final String getDeviceName(UserAgent userAgent) {
    return userAgent.getValue(UserAgent.DEVICE_NAME);
  }

  /**
   * 获取设备类型
   *
   * @param userAgent
   * @return
   * @throws IOException
   */
  public final String getDeviceClass(UserAgent userAgent) {
    return userAgent.getValue(UserAgent.DEVICE_CLASS);
  }

  /**
   * 获取系统名称
   *
   * @param userAgent
   * @return
   * @throws IOException
   */
  public final String getOSNameVersion(UserAgent userAgent) {
    return userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME_VERSION);
  }

  /**
   * 获取userAgentVO对象
   *
   * @param request
   * @return
   */
  public final com.steven.solomon.utils.userAgent.entity.UserAgent getUserAgentVO(HttpServletRequest request) {
    String agent = getAgentString(request);
    return getUserAgentVO(agent);
  }

  /**
   * 获取设备厂商
   *
   * @param userAgent 设备Model
   * @return 返回厂商
   * @throws IOException 读写异常
   */
  public final String getDeviceBrand(UserAgent userAgent) {
    return userAgent.getValue(UserAgent.DEVICE_BRAND);
  }


  /**
   * 获取userAgentVO对象
   *
   * @param agent userAgent字符串
   * @return UserAgentModel
   */
  public final com.steven.solomon.utils.userAgent.entity.UserAgent getUserAgentVO(String agent) {
    UserAgent                                         userAgent = userAgentAnalyzer.parse(agent);
    com.steven.solomon.utils.userAgent.entity.UserAgent agentVO   = new com.steven.solomon.utils.userAgent.entity.UserAgent();
    agentVO.setDevice(getDeviceName(userAgent));
    agentVO.setDeviceType(getDeviceClass(userAgent));
    agentVO.setDeviceBrand(getDeviceBrand(userAgent));
    agentVO.setBrowserNameVersion(getBrowserNameVersion(userAgent));
    agentVO.setOsNameVersion(getOSNameVersion(userAgent));
    agentVO.setUserAgent(agent);
    return agentVO;
  }

  public static void main(String[] args) {
    com.steven.solomon.utils.userAgent.entity.UserAgent userAgent = new UserAgentUtils().getUserAgentVO(
          "Mozilla/5.0 (Linux; Android 9; EVR-L29 Build/HUAWEIEVR-L29; xx-xx) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/70.0.3538.110 Mobile Safari/537.36"
      );
      System.out.println("系统以及版本:" + userAgent.getOsNameVersion());
      System.out.println("型号:" + userAgent.getDevice());
      System.out.println("设备厂商:"+userAgent.getDeviceBrand());
      System.out.println("设备类型:"+userAgent.getDeviceType());
      System.out.println("浏览器名称以及版本号:" + userAgent.getBrowserNameVersion());
  }
}
