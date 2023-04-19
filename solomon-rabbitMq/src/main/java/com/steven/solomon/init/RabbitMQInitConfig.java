package com.steven.solomon.init;

import com.steven.solomon.config.RabbitMQListenerConfig;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
@DependsOn("springUtil")
public class RabbitMQInitConfig implements CommandLineRunner {

    /**
     * 所有的队列监听容器MAP
     */
    public final static Map<String, AbstractMessageListenerContainer> allQueueContainerMap = new ConcurrentHashMap<>();

    @Override
    public void run(String... args) throws Exception {
        new RabbitMQListenerConfig().init();
    }
}
