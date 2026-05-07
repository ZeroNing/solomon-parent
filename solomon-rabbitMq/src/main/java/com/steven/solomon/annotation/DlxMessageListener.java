package com.steven.solomon.annotation;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.config.RabbitCondition;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rabbitmq标注注解
 */
@Target(value = {ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@Conditional(RabbitCondition.class)
public @interface DlxMessageListener {

    @AliasFor(annotation = Component.class)
    String value() default StrUtil.EMPTY;
}
