package com.steven.solomon.job.power.config;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * PowerJob 条件匹配器。
 *
 * <p>根据配置 {@code powerjob.worker.enabled} 判断是否启用 PowerJob 组件。
 * 当值为 {@code false} 时，PowerJob 相关的 Bean 不会被注册。</p>
 */
public class PowerJobCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 从环境配置中读取 powerjob.worker.enabled，默认 true
        String enabled = ObjectUtil.defaultIfNull(context.getEnvironment().getProperty("powerjob.worker.enabled"),"true");
        return BooleanUtil.toBoolean(enabled);
    }
}
