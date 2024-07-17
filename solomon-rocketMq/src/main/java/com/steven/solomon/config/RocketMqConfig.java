package com.steven.solomon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.steven.solomon.config.annotation.RocketMqProducer;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RocketMqConfig implements CommandLineRunner {

    private final Logger logger = LoggerUtils.logger(getClass());

    private RocketMqProducer producer;

    public RocketMqConfig(ApplicationContext applicationContext) {
        SpringUtil.setContext(applicationContext);
    }
    /**
     * 解决RocketMQ Jackson不支持Java时间类型配置
     * 源码参考：{@link org.apache.rocketmq.spring.autoconfigure.MessageConverterConfiguration}
     */
    @Bean
    @ConditionalOnMissingBean(RocketMQMessageConverter.class)
    public RocketMQMessageConverter createRocketMQMessageConverter() {
        RocketMQMessageConverter converter = new RocketMQMessageConverter();
        CompositeMessageConverter compositeMessageConverter = (CompositeMessageConverter) converter.getMessageConverter();
        List<MessageConverter> messageConverterList = compositeMessageConverter.getConverters();
        for (MessageConverter messageConverter : messageConverterList) {
            if (messageConverter instanceof MappingJackson2MessageConverter) {
                MappingJackson2MessageConverter jackson2MessageConverter = (MappingJackson2MessageConverter) messageConverter;
                ObjectMapper objectMapper = jackson2MessageConverter.getObjectMapper();
                // 增加Java8时间模块支持，实体类可以传递LocalDate/LocalDateTime
                objectMapper.registerModules(new JavaTimeModule());
            }
        }
        return converter;
    }


    @Override
    public void run(String... args) throws Exception {
        //根据RocketMqProducer注解找出使用这个注解的类并初始化生产者
        this.initProducer(new ArrayList<>(SpringUtil.getBeansWithAnnotation(RocketMqProducer.class).values()));
    }

    private void initProducer(List<Object> clazzList)throws Exception{
        // 判断消费者队列是否存在
        if (ValidateUtils.isEmpty(clazzList)) {
            logger.debug("RocketMqConfig:没有rocketMq生产者");
            return;
        }
        // 遍历生产者进行初始化
        for (Object abstractConsumer : clazzList) {
            // 根据反射获取RocketMqProducer注解信息
            producer = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), RocketMqProducer.class);
            // 生产者组名称
            String producerGroup = SpringUtil.getElValue(producer.producerGroup());
            // 生产者组下实例名称
            String instanceName = SpringUtil.getElValue(producer.instanceName());
            if(ValidateUtils.isEmpty(producerGroup)){
               logger.error("{}类的RocketMqProducer注解的生产者的名称为空,不注册生产者",abstractConsumer.getClass().getSimpleName());
               continue;
            }
            if(ValidateUtils.isEmpty(instanceName)){
                logger.error("{}类的RocketMqProducer注解的实例名称为空,不注册生产者",abstractConsumer.getClass().getSimpleName());
                continue;
            }
            DefaultMQProducer defaultMQProducer = new DefaultMQProducer(producerGroup);
            defaultMQProducer.setInstanceName(instanceName);
            defaultMQProducer.setDefaultTopicQueueNums(producer.defaultTopicQueueNums());
            defaultMQProducer.setCreateTopicKey(producer.createTopicKey());
            defaultMQProducer.setSendMsgTimeout(producer.sendMsgTimeout());
            defaultMQProducer.setRetryTimesWhenSendFailed(producer.retryTimesWhenSendFailed());
            defaultMQProducer.setRetryTimesWhenSendAsyncFailed(producer.retryTimesWhenSendAsyncFailed());
            defaultMQProducer.setMaxMessageSize(producer.maxMessageSize());
            defaultMQProducer.setCompressMsgBodyOverHowmuch(producer.compressMsgBodyOverHowmuch());
            defaultMQProducer.setUseTLS(producer.useTLS());
            defaultMQProducer.setSendMessageWithVIPChannel(producer.sendMessageWithVIPChannel());
            defaultMQProducer.start();
        }
    }
}
