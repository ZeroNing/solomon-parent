package com.steven.solomon.pojo.enums;

public enum OrderByEnum implements BaseEnum<String>{
    DESCEND("DESC","降序"),
    ASC("ASC","升序"),
    ;

    private String label;

    private String desc;

    OrderByEnum(String label,String desc) {
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
