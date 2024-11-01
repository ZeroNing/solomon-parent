package com.steven.solomon.config;

import cn.hutool.core.util.BooleanUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RabbitCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = ValidateUtils.getOrDefault(context.getEnvironment().getProperty("spring.rabbitmq.enabled"),"true");
        return BooleanUtil.toBoolean(enabled);
    }
}
