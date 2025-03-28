package com.steven.solomon.config;

import cn.hutool.core.util.BooleanUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class XxlJobCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 从环境配置中读取 "my.feature.enabled" 属性
        String enabled = ValidateUtils.getOrDefault(context.getEnvironment().getProperty("xxl.enabled"),"true");
        // 返回属性值是否为 "true"
        return BooleanUtil.toBoolean(enabled);
    }
}
