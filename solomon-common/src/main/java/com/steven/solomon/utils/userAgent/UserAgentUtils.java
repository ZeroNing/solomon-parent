package com.steven.solomon.utils.userAgent;

import com.steven.solomon.verification.ValidateUtils;
import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Component;

@Component
public class UserAgentUtils {

  private UserAgentAnalyzer userAgentAnalyzer = null;

  public UserAgentUtils(){
    if(ValidateUtils.isEmpty(this.userAgentAnalyzer)){
      this.userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
          .withFields(UserAgent.AGENT_NAME_VERSION, UserAgent.OPERATING_SYSTEM_NAME_VERSION, UserAgent.DEVICE_BRAND, UserAgent.DEVICE_CLASS, UserAgent.DEVICE_NAME)
          .hideMatcherLoadStats().withCache(10000).build();
    }
  }

  /**
   * 获取agent字符串
   */
  public String getAgentString(HttpServletRequest request) {
      return request.getHeader("User-Agent");
  }

  /**
   * 获取浏览器名称跟版本号
   */
  public String getBrowserNameVersion(UserAgent userAgent) {
    return userAgent.getValue(UserAgent.AGENT_NAME_VERSION);
  }

  /**
   * 获取设备名称
   */
  public String getDeviceName(UserAgent userAgent) {
    return userAgent.getValue(UserAgent.DEVICE_NAME);
  }

  /**
   * 获取设备类型
   */
  public String getDeviceClass(UserAgent userAgent) {
    return userAgent.getValue(UserAgent.DEVICE_CLASS);
  }

  /**
   * 获取系统名称
   */
  public String getOSNameVersion(UserAgent userAgent) {
    return userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME_VERSION);
  }

  /**
   * 获取userAgentVO对象
   */
  public com.steven.solomon.utils.userAgent.entity.UserAgent getUserAgentVO(HttpServletRequest request) {
    String agent = getAgentString(request);
    return getUserAgentVO(agent);
  }

  /**
   * 获取设备厂商
   */
  public String getDeviceBrand(UserAgent userAgent) {
    return userAgent.getValue(UserAgent.DEVICE_BRAND);
  }


  /**
   * 获取userAgentVO对象
   */
  public com.steven.solomon.utils.userAgent.entity.UserAgent getUserAgentVO(String agent) {
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
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
      );
      System.out.println("系统以及版本:" + userAgent.getOsNameVersion());
      System.out.println("型号:" + userAgent.getDevice());
      System.out.println("设备厂商:"+userAgent.getDeviceBrand());
      System.out.println("设备类型:"+userAgent.getDeviceType());
      System.out.println("浏览器名称以及版本号:" + userAgent.getBrowserNameVersion());
  }
}
