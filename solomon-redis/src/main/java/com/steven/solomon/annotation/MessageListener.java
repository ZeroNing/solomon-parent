package com.steven.solomon.annotation;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.enums.TopicMode;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis消息队列
 */
@Target(value = {ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface MessageListener {

    @AliasFor(annotation = Component.class)
    String value() default StrUtil.EMPTY;

    /**
     * 主题名
     */
    String topic();

    /**
     * 主题模式
     */
    TopicMode mode() default TopicMode.CHANNEL;
}
