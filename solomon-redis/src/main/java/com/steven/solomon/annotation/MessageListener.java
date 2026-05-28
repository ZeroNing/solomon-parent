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
 * Redis消息队列监听器注解。
 *
 * <p>标注在类上，将该类注册为Redis消息监听器，自动订阅指定主题。
 * 被标注的类需实现 {@link org.springframework.data.redis.connection.MessageListener} 接口。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code @MessageListener(topic = "order.created", mode = TopicMode.CHANNEL)}
 * public class OrderCreatedConsumer extends AbstractConsumer&lt;Order, Void&gt; {
 *     // ...
 * }
 * </pre>
 */
@Target(value = {ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface MessageListener {

    /**
     * 组件名称，等同于 {@link Component#value()}。
     */
    @AliasFor(annotation = Component.class)
    String value() default StrUtil.EMPTY;

    /**
     * 主题名，支持SpEL表达式。
     */
    String topic();

    /**
     * 主题模式，默认为CHANNEL（精确匹配）。
     * @see TopicMode
     */
    TopicMode mode() default TopicMode.CHANNEL;
}
