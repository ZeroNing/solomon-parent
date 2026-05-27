package com.steven.solomon.entity;

import java.io.Serializable;
import java.util.List;

public class JobComponentUserRoleInfoVO implements Serializable {

    private List<Integer> observer;
    private List<Integer> qa;
    private List<Integer> developer;
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
