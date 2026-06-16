package com.steven.solomon.job.power.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * PowerJob 命名空间视图对象（简化版）。
 *
 * <p>与 {@link JobNamespace} 不同，本类使用 {@link java.util.Date} 而非 LocalDateTime，
 * 且不包含 token、componentUserRoleInfo 等敏感或内部字段，主要用于 API 响应中的命名空间信息展示。</p>
 */
public class JobNamespaceVO implements Serializable {

    /** 主键 ID。 */
    private int id;
    /** 命名空间编码。 */
    private String code;
    /** 命名空间名称。 */
    private String name;
    /** 所属部门。 */
    private String dept;
    /** 标签。 */
    private String tags;
    /** 扩展信息。 */
    private String extra;
    /** 状态。 */
    private int status;
    /** 状态字符串描述。 */
    private String statusStr;
    /** 创建时间。 */
    private Date gmtCreate;
    /** 创建时间字符串。 */
    private String gmtCreateStr;
    /** 修改时间。 */
    private Date gmtModified;
    /** 修改时间字符串。 */
    private String gmtModifiedStr;
    /** 显示名称。 */
    private String showName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusStr() {
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getGmtCreateStr() {
        return gmtCreateStr;
    }

    public void setGmtCreateStr(String gmtCreateStr) {
        this.gmtCreateStr = gmtCreateStr;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getGmtModifiedStr() {
        return gmtModifiedStr;
    }

    public void setGmtModifiedStr(String gmtModifiedStr) {
        this.gmtModifiedStr = gmtModifiedStr;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }
}
