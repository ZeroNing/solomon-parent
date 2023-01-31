package com.steven.solomon.holder;

import com.steven.solomon.verification.ValidateUtils;
import java.io.Serializable;

public class HeardHolder {

  private static final ThreadLocal<TenantHeard> tenantHeardThreadLocal = ThreadLocal.withInitial(() -> {
    TenantHeard header = new TenantHeard();
    return header;
  });

  private static final ThreadLocal<RequestHeader> requestHeadThreadLocal = ThreadLocal.withInitial(() -> {
    RequestHeader header = new RequestHeader();
    return header;
  });

  public static String getTenantId(){
    TenantHeard tenantHeard = tenantHeardThreadLocal.get();
    return ValidateUtils.getOrDefault(tenantHeard.getTenantId(),"");
  }

  public static String getTenantCode(){
    TenantHeard tenantHeard = tenantHeardThreadLocal.get();
    return ValidateUtils.getOrDefault(tenantHeard.getTenantCode(),"");
  }

  public static String getTenantName(){
    TenantHeard tenantHeard = tenantHeardThreadLocal.get();
    return ValidateUtils.getOrDefault(tenantHeard.getTenantName(),"");
  }

  public static void setTenantId(String tenantId){
    TenantHeard tenantHeard = tenantHeardThreadLocal.get();
    tenantHeard.setTenantId(tenantId);
  }

  public static void setTenantCode(String tenantCode){
    TenantHeard tenantHeard = tenantHeardThreadLocal.get();
    tenantHeard.setTenantCode(tenantCode);
  }

  public static void setTenantName(String tenantName){
    TenantHeard tenantHeard = tenantHeardThreadLocal.get();
    tenantHeard.setTenantName(tenantName);
  }

  public static String getUserId(){
    RequestHeader requestHeader = requestHeadThreadLocal.get();
    return ValidateUtils.getOrDefault(requestHeader.getUserId(),"");
  }

  public static String getUserCode(){
    RequestHeader requestHeader = requestHeadThreadLocal.get();
    return ValidateUtils.getOrDefault(requestHeader.getUserCode(),"");
  }

  public static String getUserName(){
    RequestHeader requestHeader = requestHeadThreadLocal.get();
    return ValidateUtils.getOrDefault(requestHeader.getUserName(),"");
  }

  public static String getToken(){
    RequestHeader requestHeader = requestHeadThreadLocal.get();
    return ValidateUtils.getOrDefault(requestHeader.getToken(),"");
  }

  public static void setUserId(String userId){
    RequestHeader requestHeader = requestHeadThreadLocal.get();
    requestHeader.setUserId(userId);
  }

  public static void setUserCode(String userCode){
    RequestHeader requestHeader = requestHeadThreadLocal.get();
    requestHeader.setUserCode(userCode);
  }

  public static void setUserName(String userName){
    RequestHeader requestHeader = requestHeadThreadLocal.get();
    requestHeader.setUserName(userName);
  }

  public static void setToken(String token){
    RequestHeader requestHeader = requestHeadThreadLocal.get();
    requestHeader.setToken(token);
  }

  public static class RequestHeader implements Serializable {
    private String userId;

    private String userCode;

    private String userName;

    private String token;

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public String getUserCode() {
      return userCode;
    }

    public void setUserCode(String userCode) {
      this.userCode = userCode;
    }

    public String getUserName() {
      return userName;
    }

    public void setUserName(String userName) {
      this.userName = userName;
    }

    public String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }
  }

  public static class TenantHeard implements Serializable {

    /**
     * SAAS租户id
     */
    private String tenantId;
    /**
     * SAAS租户名称
     */
    private String tenantName;
    /**
     * SAAS租户编码
     */
    private String tenantCode;

    public String getTenantId() {
      return tenantId;
    }

    public void setTenantId(String tenantId) {
      this.tenantId = tenantId;
    }

    public String getTenantName() {
      return tenantName;
    }

    public void setTenantName(String tenantName) {
      this.tenantName = tenantName;
    }

    public String getTenantCode() {
      return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
      this.tenantCode = tenantCode;
    }
  }
}
