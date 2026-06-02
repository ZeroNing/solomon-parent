package com.steven.solomon.clamav.config;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * ClamAV 病毒扫描启用条件判断。
 * <p>
 * 实现 Spring {@link Condition} 接口，根据配置文件中 {@code clamav.enabled} 属性的值
 * 判断是否启用 ClamAV 病毒扫描功能。
 * 默认值为 {@code false}，即不启用。
 * </p>
 *
 * @author 创建者
 */
public class ClamAvCondition implements Condition {

    /**
     * 判断条件是否匹配。
     * <p>
     * 从 Spring 环境变量中读取 {@code clamav.enabled} 配置项，
     * 如果未配置则默认返回 {@code false}。
     * </p>
     *
     * @param context  条件上下文，用于获取环境配置
     * @param metadata 注解类型的元数据
     * @return 如果 {@code clamav.enabled} 为 {@code true} 则返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = ObjectUtil.defaultIfNull(context.getEnvironment().getProperty("clamav.enabled"),"false");
        return BooleanUtil.toBoolean(enabled);
    }
}
