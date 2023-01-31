package com.steven.solomon.utils.userAgent.entity;

import java.io.Serializable;

/**
 * 设备型号基类
 */
public class UserAgent implements Serializable {

  private static final long serialVersionUID = -400534158145437668L;

  /**
   * 设备名称
   */
  private String device;
  /**
   * 设备类型
   */
  private String deviceType;
  /**
   * 浏览器名称跟版本号
   */
  private String browserNameVersion;
  /**
   * 系统名称以及版本
   */
  private String osNameVersion;
  /**
   * userAgent字符串
   */
  private String userAgent;
  /**
   * 设备厂商
   */
  private String deviceBrand;

  public UserAgent() {
    super();
  }

  public String getDevice() {
    return device;
  }

  public void setDevice(String device) {
    this.device = device;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getDeviceBrand() {
    return deviceBrand;
  }

  public void setDeviceBrand(String deviceBrand) {
    this.deviceBrand = deviceBrand;
  }

  public String getOsNameVersion() {
    return osNameVersion;
  }

  public void setOsNameVersion(String osNameVersion) {
    this.osNameVersion = osNameVersion;
  }

  public String getBrowserNameVersion() {
    return browserNameVersion;
  }

  public void setBrowserNameVersion(String browserNameVersion) {
    this.browserNameVersion = browserNameVersion;
  }
}