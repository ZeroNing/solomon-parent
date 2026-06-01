package com.steven.solomon.clamav.config;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ClamAvCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = ObjectUtil.defaultIfNull(context.getEnvironment().getProperty("clamav.enabled"),"false");
        return BooleanUtil.toBoolean(enabled);
    }
}
