package com.steven.solomon.job.power.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PowerJob 命名空间（Namespace）实体。
 *
 * <p>映射 PowerJob 管理端的命名空间信息，包含编码、名称、状态、令牌等。
 * 命名空间是 PowerJob 中隔离不同业务应用的逻辑单元。</p>
 */
public class JobNamespace implements Serializable {

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
    private LocalDateTime gmtCreate;
    /** 创建时间字符串。 */
    private String gmtCreateStr;
    /** 修改时间。 */
    private LocalDateTime gmtModified;
    /** 修改时间字符串。 */
    private String gmtModifiedStr;
    /** 显示名称。 */
    private String showName;
    /** 命名空间令牌。 */
    private String token;
    /** 组件用户角色信息。 */
    private ComponentUserRoleInfo componentUserRoleInfo;
    /** 创建人显示名称。 */
    private String creatorShowName;
    /** 修改人显示名称。 */
    private String modifierShowName;

    // Getters and Setters

    /**
     * PowerJob 命名空间组件用户角色信息。
     *
     * <p>以字符串列表形式存储各角色的用户名，而非用户 ID。
     * 用于命名空间级别的权限配置。</p>
     */
    public static class ComponentUserRoleInfo {

        /** 观察者用户名列表。 */
        private List<String> observer;
        /** QA 用户名列表。 */
        private List<String> qa;
        /** 开发者用户名列表。 */
        private List<String> developer;
        /** 管理员用户名列表。 */
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
