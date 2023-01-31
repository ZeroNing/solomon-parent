package com.steven.solomon.model;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.steven.solomon.annotation.JsonEnum;
import com.steven.solomon.date.DateTimeUtils;
import com.steven.solomon.enums.DelFlagEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * ALTER TABLE `tenancy`.`*`
 * ADD COLUMN `create_id` varchar(50) NOT NULL COMMENT '创建人id' AFTER `id`,
 * ADD COLUMN `create_date` datetime NOT NULL COMMENT '创建时间' AFTER `create_id`,
 * ADD COLUMN `update_id` varchar(50) NOT NULL COMMENT '更新人' AFTER `create_date`,
 * ADD COLUMN `update_date` datetime NOT NULL COMMENT '更新时间' AFTER `update_id`,
 * ADD COLUMN `remark` varchar(255) NULL COMMENT '备注' AFTER `update_date`,
 * ADD COLUMN `del_flag` char(1) NOT NULL DEFAULT 0 COMMENT '删除标记' AFTER `remark`;
 */
public class BaseEntity<I> implements Serializable {

  private static final long          serialVersionUID = 1L;

  /**
   * 主键id
   */
  private              I        id;

  /**
   * 创建人id
   */
  private              I        createId;

  /**
   * 创建时间
   */
  @TableField(fill = FieldFill.INSERT)
  private              LocalDateTime createDate;

  /**
   * 更新人id
   */
  private              I        updateId;

  /**
   * 更新时间
   */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private              LocalDateTime updateDate;
  /**
   * 删除标记
   */
  @JsonEnum(enumClass = DelFlagEnum.class)
  @TableLogic(value = "0",delval = "1")
  private              String        delFlag;

  /**
   * 备注
   */
  private              String        remark;

  public BaseEntity() {
    super();
  }

  public void create() {
    this.createDate = DateTimeUtils.getLocalDateTime();
    this.updateDate = DateTimeUtils.getLocalDateTime();
    this.delFlag    = DelFlagEnum.NOT_DELETE.Value();
  }

  public void create(I createId) {
    this.create();
    this.createId = createId;
    this.updateId   = createId;
  }

  public void update(I updateId) {
    this.updateId = updateId;
    update();
  }

  public void update() {
    this.updateDate = DateTimeUtils.getLocalDateTime();
  }

  public void delete(I updateId) {
    this.updateId = updateId;
    delete();
  }

  public void delete() {
    this.updateDate = DateTimeUtils.getLocalDateTime();
    this.delFlag    = DelFlagEnum.DELETE.Value();
  }

  public LocalDateTime getCreateDate() {
    return createDate;
  }

  public void setCreateDate(LocalDateTime createDate) {
    this.createDate = createDate;
  }

  public LocalDateTime getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(LocalDateTime updateDate) {
    this.updateDate = updateDate;
  }

  public String getDelFlag() {
    return delFlag;
  }

  public void setDelFlag(String delFlag) {
    this.delFlag = delFlag;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public I getId() {
    return id;
  }

  public void setId(I id) {
    this.id = id;
  }

  public I getCreateId() {
    return createId;
  }

  public void setCreateId(I createId) {
    this.createId = createId;
  }

  public I getUpdateId() {
    return updateId;
  }

  public void setUpdateId(I updateId) {
    this.updateId = updateId;
  }
}
