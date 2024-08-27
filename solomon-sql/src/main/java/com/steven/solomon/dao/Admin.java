package com.steven.solomon.dao;

import com.steven.solomon.annotation.Column;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Admin {

    @Column
    private String id;

    @Column
    private String name;

    @Column
    private boolean flag;

    @Column
    private BigDecimal switchNum;

    @Column
    private LocalDateTime date;

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public BigDecimal getSwitchNum() {
        return switchNum;
    }

    public void setSwitchNum(BigDecimal switchNum) {
        this.switchNum = switchNum;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
