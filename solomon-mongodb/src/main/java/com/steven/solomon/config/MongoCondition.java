package com.steven.solomon.config;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MongoCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment().getProperty("spring.data.mongodb.enabled");
        return BooleanUtil.toBoolean(enabled);
    }
}
