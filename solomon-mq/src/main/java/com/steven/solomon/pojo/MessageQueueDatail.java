package com.steven.solomon.pojo;

import java.io.Serializable;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.util.ObjectUtils;

public class MessageQueueDatail implements Serializable {

	private static final long serialVersionUID = 7292656135434186436L;
	/**
	 * 队列名称
	 */
	private String queueName;

	/**
	 * 监听容器标识
	 */
	private String containerIdentity;

	/**
	 * 监听是否有效
	 */
	private boolean activeContainer;

	/**
	 * 是否正在监听
	 */
	private boolean running;

	/**
	 * 活动消费者数量
	 */
//	private int activeConsumerCount;

	public MessageQueueDatail(String queueName, AbstractMessageListenerContainer container) {
		this.queueName = queueName;
		this.running = container.isRunning();
		this.activeContainer = container.isActive();
//		this.activeConsumerCount = container.;
		this.containerIdentity = "Container@" + ObjectUtils.getIdentityHexString(container);
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getContainerIdentity() {
		return containerIdentity;
	}

	public void setContainerIdentity(String containerIdentity) {
		this.containerIdentity = containerIdentity;
	}

	public boolean isActiveContainer() {
		return activeContainer;
	}

	public void setActiveContainer(boolean activeContainer) {
		this.activeContainer = activeContainer;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
