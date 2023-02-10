package com.steven.solomon.holder;

import com.steven.solomon.verification.ValidateUtils;
import java.io.Serializable;
import org.springframework.web.bind.annotation.RequestHeader;

public class HeardHolder {

  private static final ThreadLocal<TenantHeard> tenantHeardThreadLocal = ThreadLocal.withInitial(() -> {
    TenantHeard header = new TenantHeard();
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
