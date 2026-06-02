package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * XXL-JOB GLUE 运行模式枚举。
 *
 * <p>定义任务以何种代码模式运行。Bean 模式指向 Spring Bean 中的方法，
 * GLUE 模式使用管理端在线编辑的脚本（支持 Java/Shell/Python 等）。</p>
 *
 * @author xuxueli 17/4/26
 */
public enum GlueTypeEnum implements BaseEnum<String>  {

    /** Bean 模式：任务作为 Spring Bean 执行。 */
    BEAN("BEAN"),
    /** GLUE(Java)：使用管理端在线编辑的 Java 代码。 */
    GLUE_GROOVY("GLUE(Java)"),
    /** GLUE(Shell)：使用管理端在线编辑的 Shell 脚本。 */
    GLUE_SHELL("GLUE(Shell)"),
    /** GLUE(Python)：使用管理端在线编辑的 Python 脚本。 */
    GLUE_PYTHON("GLUE(Python)"),
    /** GLUE(Php)：使用管理端在线编辑的 PHP 脚本。 */
    GLUE_PHP("GLUE(Php)"),
    /** GLUE(Nodejs)：使用管理端在线编辑的 Node.js 脚本。 */
    GLUE_NODEJS("GLUE(Nodejs)"),
    /** GLUE(PowerShell)：使用管理端在线编辑的 PowerShell 脚本。 */
    GLUE_POWERSHELL("GLUE(PowerShell)");

    /** GLUE 类型的中文描述。 */
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
