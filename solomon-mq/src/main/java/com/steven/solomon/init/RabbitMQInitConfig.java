package com.steven.solomon.init;

import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.config.RabbitMQListenerConfig;
import com.steven.solomon.enums.MqChoiceEnum;
import com.steven.solomon.exception.BaseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@DependsOn("springUtil")
public class RabbitMQInitConfig implements CommandLineRunner {

    @Value("${mq.choice}")
    private MqChoiceEnum choice;

    /**
     * 所有的队列监听容器MAP
     */
    public final static Map<String, AbstractMessageListenerContainer> allQueueContainerMap = new ConcurrentHashMap<>();

    @Override
    public void run(String... args) throws Exception {
        switch (choice){
            case RABBIT:
                new RabbitMQListenerConfig().init();
            case ACTIVE:
                break;
            case KAFKA:
                break;
            default:
                throw new BaseException(MqErrorCode.NO_CORRESPONDING_IMPLEMENTATION);
        }

    }
}
