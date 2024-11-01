package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * LogType
 *
 * @author tjq
 * @since 2022/10/3
 */
public enum LogType implements BaseEnum<Integer> {

    /**
     * Online logging.
     * ONLINE：在线日志，通常指直接发送到远程日志服务器或在线服务。
     */
    ONLINE(1),

    /**
     * Local logging.
     * LOCAL：本地日志，日志记录在本地存储设备上。
     */
    LOCAL(2),

    /**
     * Standard output logging.
     * STDOUT：标准输出，日志直接输出到控制台或标准输出流。
     */
    STDOUT(3),

    /**
     * Local and online logging.
     * LOCAL_AND_ONLINE：同时记录本地和在线日志。
     */
    LOCAL_AND_ONLINE(4),

    /**
     * Null logging.
     * NULL：无日志记录，关闭日志记录功能。
     */
    NULL(999);

    // 用于存储日志类型的整数标签
    private final Integer label;

    /**
     * 构造函数，初始化枚举值的标签。
     *
     * @param label 用于标识日志类型的整数标签。
     */
    LogType(Integer label) {
        this.label = label;
    }

    /**
     * 获取枚举值的描述，与枚举名称相同。
     *
     * @return 枚举的名称。
     */
    @Override
    public String getDesc() {
        return this.name();
    }

    /**
     * 获取枚举值的标签。
     *
     * @return 整数形式的标签。
     */
    @Override
    public Integer label() {
        return this.label;
    }

    /**
     * 获取枚举值的键，与枚举名称相同。
     *
     * @return 枚举的名称。
     */
    @Override
    public String key() {
        return this.name();
    }
}