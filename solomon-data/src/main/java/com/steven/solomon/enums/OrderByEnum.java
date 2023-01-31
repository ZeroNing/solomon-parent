package com.steven.solomon.enums;

public enum OrderByEnum implements BaseEnum{
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
