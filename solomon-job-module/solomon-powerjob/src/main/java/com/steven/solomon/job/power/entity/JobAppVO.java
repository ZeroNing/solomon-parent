package com.steven.solomon.job.power.entity;

import java.util.Date;

/**
 * PowerJob 应用（App）视图对象。
 *
 * <p>映射 PowerJob 管理端应用信息，用于查询和创建应用时与 Admin API 交互。
 * 包含应用名称、所属命名空间、创建人等信息。</p>
 */
public class JobAppVO {

    /** 应用主键 ID。 */
    private int id;
    /** 应用名称。 */
    private String appName;
    /** 所属命名空间 ID。 */
    private int namespaceId;
    /** 应用标题。 */
    private String title;
    /** 应用密码。 */
    private String password;
    /** 标签。 */
    private String tags;
    /** 扩展信息。 */
    private String extra;
    /** 组件用户角色信息。 */
    private JobComponentUserRoleInfoVO componentUserRoleInfo;
    /** 创建时间。 */
    private Date gmtCreate;
    /** 创建时间字符串。 */
    private String gmtCreateStr;
    /** 修改时间。 */
    private Date gmtModified;
    /** 修改时间字符串。 */
    private String gmtModifiedStr;
    /** 创建人显示名称。 */
    private String creatorShowName;
    /** 修改人显示名称。 */
    private String modifierShowName;
    /** 命名空间对象。 */
    private JobNamespaceVO namespace;
    /** 命名空间名称。 */
    private String namespaceName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(int namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public JobComponentUserRoleInfoVO getComponentUserRoleInfo() {
        return componentUserRoleInfo;
    }

    public void setComponentUserRoleInfo(JobComponentUserRoleInfoVO componentUserRoleInfo) {
        this.componentUserRoleInfo = componentUserRoleInfo;
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

    public String getCreatorShowName() {
        return creatorShowName;
    }

    public void setCreatorShowName(String creatorShowName) {
        this.creatorShowName = creatorShowName;
    }

    public String getModifierShowName() {
        return modifierShowName;
    }

    public void setModifierShowName(String modifierShowName) {
        this.modifierShowName = modifierShowName;
    }

    public JobNamespaceVO getNamespace() {
        return namespace;
    }

    public void setNamespace(JobNamespaceVO namespace) {
        this.namespace = namespace;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }
}
