package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

public enum DbTypeEnums implements BaseEnum<String> {
    MYSQL("mysql","mysql数据库"),
    SQL_SERVER("sqlServer","sqlServer数据库"),
    ORACLE("oracle","oracle数据库"),
    POSTGRESQL("postgreSql","postgreSql数据库"),;

    private String label;

    private String desc;

    DbTypeEnums(String label,String desc) {
        this.label = label;
        this.desc = desc;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String label() {
        return this.label;
    }

    @Override
    public String key() {
        return this.name();
    }
}
