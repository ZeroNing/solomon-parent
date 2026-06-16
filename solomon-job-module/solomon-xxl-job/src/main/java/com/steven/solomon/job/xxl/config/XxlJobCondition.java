package com.steven.solomon.job.xxl.config;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * XXL-JOB 条件匹配器。
 *
 * <p>根据配置 {@code xxl.enabled} 判断是否启用 XXL-JOB 组件。
 * 当值为 {@code false} 时，XXL-JOB 相关的 Bean 不会被注册。</p>
 */
public class XxlJobCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 从环境配置中读取 xxl.enabled，默认 true
        String enabled = ObjectUtil.defaultIfNull(context.getEnvironment().getProperty("xxl.enabled"),"true");
        return BooleanUtil.toBoolean(enabled);
    }
}
