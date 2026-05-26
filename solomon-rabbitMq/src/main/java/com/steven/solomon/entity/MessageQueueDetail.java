package com.steven.solomon.entity;

import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;

import java.io.Serializable;

/**
 * RabbitMQ 队列监听容器快照。
 *
 * <p>该对象只承载管理端展示所需的队列名称、容器状态和容器标识，不暴露 Spring
 * 容器对象本身，避免序列化时把运行时资源带出去。</p>
 */
public class MessageQueueDetail implements Serializable {

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

    public MessageQueueDetail(String queueName, AbstractMessageListenerContainer container) {
        this.queueName = queueName;
        this.running = container.isRunning();
        this.activeContainer = container.isActive();
        // 使用 JVM 对象身份哈希生成轻量标识，避免引入 Spring 工具类做简单字符串拼接。
        this.containerIdentity = "Container@" + Integer.toHexString(System.identityHashCode(container));
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
