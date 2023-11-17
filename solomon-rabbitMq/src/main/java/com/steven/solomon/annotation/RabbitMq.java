package com.steven.solomon.annotation;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rabbitmq标注注解
 * @author huangweihua
 */
@Target(value = { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RabbitMq {

	@AliasFor(annotation = Component.class)
	String value() default "";

	/**
	 * 队列
	 */
	String[] queues();

	/**
	 * 交换器
	 */
	String exchange() default "";

	/**
	 * 路由规则
	 */
	String routingKey() default "";

	/**
	 * 是否持久化
	 */
	boolean isPersistence() default true;

	/**
	 * 确认模式（只支持手动提交，自动提交代码暂时不支持）
	 */
	AcknowledgeMode mode() default AcknowledgeMode.MANUAL;

	/**
	 * 每个队列消费者数量
	 */
	int consumersPerQueue() default 1;

	/**
	 * 每次的接收的消息数量最大数值(0:公平分发 1:不公平分发)
	 */
	int prefetchCount() default AbstractMessageListenerContainer.DEFAULT_PREFETCH_COUNT;

	/**
	 * 交换类型（暂时不支持system，只支持DIRECT、TOPIC、FANOUT、HEADERS）
	 */
	String exchangeTypes() default ExchangeTypes.DIRECT;

	/**
	 * 消息最大存活时间
	 */
	long delay() default 0L;

	/**
	 * 死信队列Class
	 */
	Class dlxClazz() default void.class;

	/**
	 * 是否启用插件内的ttl队列
	 */
	boolean isDelayExchange() default false;

	/**
	 * Headers交换器下需要配置 是否匹配全部头部属性 默认非全部
	 */
	boolean matchAll() default false;

	/**
	 * Headers交换器下需要配置 是否匹配值,true就是匹配值,false就是不匹配值，只判断是否存在
	 */
	boolean matchValue() default false;

	/**
	 * 需要匹配的头部消息,如matchAll为True清空则需要匹配全部headers存在,才可通过,false为只要匹配中其中一个即可通过
	 * 如果matchValue为true,headers结果应为 0:key,1:value,2:key,3:value.........如此下去,false的话则全部为key
	 * @return
	 */
	String[] headers() default {};
}
