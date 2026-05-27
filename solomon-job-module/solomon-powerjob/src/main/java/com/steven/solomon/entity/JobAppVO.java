package com.steven.solomon.entity;

import java.util.Date;

public class JobAppVO {
    private int id;
    private String appName;
    private int namespaceId;
    private String title;
    private String password;
    private String tags;
    private String extra;
    private JobComponentUserRoleInfoVO componentUserRoleInfo;
    private Date gmtCreate;
    private String gmtCreateStr;
    private Date gmtModified;
    private String gmtModifiedStr;
    private String creatorShowName;
    private String modifierShowName;
    private JobNamespaceVO namespace;
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
