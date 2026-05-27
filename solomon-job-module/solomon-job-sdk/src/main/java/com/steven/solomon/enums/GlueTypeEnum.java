package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * Created by xuxueli on 17/4/26.
 */
public enum GlueTypeEnum implements BaseEnum<String>  {

    BEAN("BEAN"),
    GLUE_GROOVY("GLUE(Java)"),
    GLUE_SHELL("GLUE(Shell)"),
    GLUE_PYTHON("GLUE(Python)"),
    GLUE_PHP("GLUE(Php)"),
    GLUE_NODEJS("GLUE(Nodejs)"),
    GLUE_POWERSHELL("GLUE(PowerShell)");

    private final String desc;

    GlueTypeEnum(String desc) {
        this.desc = desc;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String label() {
        return this.name();
    }

    @Override
    public String key() {
        return this.name();
    }

}
