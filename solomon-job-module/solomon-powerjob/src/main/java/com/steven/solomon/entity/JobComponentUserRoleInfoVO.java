package com.steven.solomon.entity;

import java.io.Serializable;
import java.util.List;

/**
 * PowerJob 组件用户角色信息视图对象。
 *
 * <p>定义 PowerJob 应用中各角色（观察者、QA、开发者、管理员）的用户 ID 列表。
 * 用于创建或更新命名空间/应用时配置权限。</p>
 */
public class JobComponentUserRoleInfoVO implements Serializable {

    /** 观察者用户 ID 列表。 */
    private List<Integer> observer;
    /** QA 用户 ID 列表。 */
    private List<Integer> qa;
    /** 开发者用户 ID 列表。 */
    private List<Integer> developer;
    /** 管理员用户 ID 列表。 */
    private List<Integer> admin;

    public List<Integer> getObserver() {
        return observer;
    }

    public void setObserver(List<Integer> observer) {
        this.observer = observer;
    }

    public List<Integer> getQa() {
        return qa;
    }

    public void setQa(List<Integer> qa) {
        this.qa = qa;
    }

    public List<Integer> getDeveloper() {
        return developer;
    }

    public void setDeveloper(List<Integer> developer) {
        this.developer = developer;
    }

    public List<Integer> getAdmin() {
        return admin;
    }

    public void setAdmin(List<Integer> admin) {
        this.admin = admin;
    }
}
