package com.steven.test.persistence.entity;


import com.steven.solomon.persistence.annotation.Column;
import com.steven.solomon.persistence.annotation.PrimaryKey;
import com.steven.solomon.persistence.annotation.Table;

import java.time.LocalDateTime;

@Table(value = Tenant.META.TABLE)
public class Tenant {

  @Column(value = META.COLUMNS.id)
  @PrimaryKey
  private Long          id;

  @Column(value = META.COLUMNS.created)
  private LocalDateTime created;

  @Column(value = META.COLUMNS.modified)
  private LocalDateTime modified;

  @Column(value = META.COLUMNS.version)
  private Integer       version;

  @Column(value = META.COLUMNS.code)
  private String code;

  @Column(value = META.COLUMNS.name)
  private String name;

  @Column(value = META.COLUMNS.disabled)
  private Boolean disabled;

  @Column(value = META.COLUMNS.expirationDate)
  private LocalDateTime expirationDate;

  @Column(value = META.COLUMNS.annualExpirationDate)
  private LocalDateTime annualExpirationDate;

  @Column(value = META.COLUMNS.permanent)
  private Boolean permanent;

  @Column(value = META.COLUMNS.remark)
  private String remark;

  @Column(value = META.COLUMNS.mode)
  private String mode;

  @Column(value = META.COLUMNS.type)
  private String type;

  @Column
  private boolean sameDbTag=false;

  @Column
  private boolean manualCreateDb=false;

  public Tenant() {
    this.created  = LocalDateTime.now();
    this.modified = LocalDateTime.now();
    this.version  = 1;
  }

  public void markUpdated() {
    this.setVersion(this.getVersion() + 1);
    this.setModified(LocalDateTime.now());
  }

  /**
   * 元数�?
   */
  public enum META {
    ;
    /**
     * 表名
     */
    public static final String TABLE = "tenant";

    /**
     * 字段
     */
    public interface COLUMNS {
      String id             = "id";
      String created        = "created";
      String modified       = "modified";
      String version        = "version";
      String code       = "code";
      String name    = "name";
      String disabled          = "disabled";
      String expirationDate        = "expiration_date";
      String annualExpirationDate = "annual_expiration_date";
      String permanent            = "permanent";
      String status = "status";
      String remark         = "remark";
      String mode = "mode";
      String type = "type";
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getCreated() {
    return created;
  }

  public void setCreated(LocalDateTime created) {
    this.created = created;
  }

  public LocalDateTime getModified() {
    return modified;
  }

  public void setModified(LocalDateTime modified) {
    this.modified = modified;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getDisabled() {
    return disabled;
  }

  public void setDisabled(Boolean disabled) {
    this.disabled = disabled;
  }

  public LocalDateTime getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(LocalDateTime expirationDate) {
    this.expirationDate = expirationDate;
  }

  public LocalDateTime getAnnualExpirationDate() {
    return annualExpirationDate;
  }

  public void setAnnualExpirationDate(LocalDateTime annualExpirationDate) {
    this.annualExpirationDate = annualExpirationDate;
  }

  public Boolean getPermanent() {
    return permanent;
  }

  public void setPermanent(Boolean permanent) {
    this.permanent = permanent;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
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

  public boolean getSameDbTag() {
    return sameDbTag;
  }

  public void setSameDbTag(boolean sameDbTag) {
    this.sameDbTag = sameDbTag;
  }

  public boolean getManualCreateDb() {
    return manualCreateDb;
  }

  public void setManualCreateDb(boolean manualCreateDb) {
    this.manualCreateDb = manualCreateDb;
  }
}
