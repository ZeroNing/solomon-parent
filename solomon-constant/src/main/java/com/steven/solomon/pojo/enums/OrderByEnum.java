package com.steven.solomon.pojo.enums;

public enum OrderByEnum implements BaseEnum<String>{
    DESCEND("DESC"),
    ASC("ASC"),
    ;

    private final String label;

    OrderByEnum(String label) {
        this.label = label;
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
