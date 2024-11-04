package com.steven.solomon.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class JobNamespace implements Serializable {

    private int id;
    private String code;
    private String name;
    private String dept;
    private String tags;
    private String extra;
    private int status;
    private String statusStr;
    private LocalDateTime gmtCreate;
    private String gmtCreateStr;
    private LocalDateTime gmtModified;
    private String gmtModifiedStr;
    private String showName;
    private String token;
    private ComponentUserRoleInfo componentUserRoleInfo;
    private String creatorShowName;
    private String modifierShowName;

    // Getters and Setters

    public static class ComponentUserRoleInfo {
        private List<String> observer;
        private List<String> qa;
        private List<String> developer;
        private List<String> admin;

        public List<String> getObserver() {
            return observer;
        }

        public void setObserver(List<String> observer) {
            this.observer = observer;
        }

        public List<String> getQa() {
            return qa;
        }

        public void setQa(List<String> qa) {
            this.qa = qa;
        }

        public List<String> getDeveloper() {
            return developer;
        }

        public void setDeveloper(List<String> developer) {
            this.developer = developer;
        }

        public List<String> getAdmin() {
            return admin;
        }

        public void setAdmin(List<String> admin) {
            this.admin = admin;
        }
// Getters and Setters
    }

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

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getGmtCreateStr() {
        return gmtCreateStr;
    }

    public void setGmtCreateStr(String gmtCreateStr) {
        this.gmtCreateStr = gmtCreateStr;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ComponentUserRoleInfo getComponentUserRoleInfo() {
        return componentUserRoleInfo;
    }

    public void setComponentUserRoleInfo(ComponentUserRoleInfo componentUserRoleInfo) {
        this.componentUserRoleInfo = componentUserRoleInfo;
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
}
