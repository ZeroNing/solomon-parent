package com.steven.solomon.config;

import cn.hutool.core.util.BooleanUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * MongoDB启用条件判断。
 *
 * <p>根据配置项 {@code spring.data.mongodb.enabled} 判断是否启用MongoDB功能，
 * 默认为true（启用）。</p>
 */
public class MongoCondition implements Condition {

    /**
     * 判断MongoDB是否启用。
     *
     * @param context  条件上下文，提供环境配置
     * @param metadata 注解类型元数据
     * @return true表示启用MongoDB，false表示禁用
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 从环境配置中读取MongoDB启用开关，默认为true
        String enabled = ValidateUtils.getOrDefault(context.getEnvironment().getProperty("spring.data.mongodb.enabled"),"true");
        return BooleanUtil.toBoolean(enabled);
    }
}
