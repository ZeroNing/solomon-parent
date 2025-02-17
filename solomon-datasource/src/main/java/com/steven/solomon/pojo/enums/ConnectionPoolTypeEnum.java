package com.steven.solomon.pojo.enums;

public enum ConnectionPoolTypeEnum {

    HIKARICP("HikariCP"),
    DRUID("Druid"),;

    private final String name;

    ConnectionPoolTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
