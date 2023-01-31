package com.steven.solomon.service;

import com.steven.solomon.annotation.RabbitMq;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

public interface BaseMQService {

	String SERVICE_NAME = "MQService";

	/**
	 * 初始化 队列 交换机 绑定
	 * 
	 * @param rabbitMq       rabbitMq注解
	 * @param queueName 	 队列名
	 * @param admin
	 * @param isInitDlxMap   是否初始化死信队列参数
	 * @param isAddDlxPrefix 是否增加死信队列前缀
	 * @return
	 */
	Queue initBinding(RabbitMq rabbitMq, String queueName, RabbitAdmin admin, boolean isInitDlxMap, boolean isAddDlxPrefix);
}
