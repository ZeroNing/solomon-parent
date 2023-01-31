package com.steven.solomon.config;

import com.steven.solomon.annotation.RabbitMq;
import com.steven.solomon.annotation.RabbitMqRetry;
import com.steven.solomon.constant.code.BaseCode;
import com.steven.solomon.init.RabbitMQInitConfig;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.profile.BaseMQProfile;
import com.steven.solomon.service.AbstractConsumer;
import com.steven.solomon.service.BaseMQService;
import com.steven.solomon.service.impl.AbstractMQService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

public class RabbitMQListenerConfig {

    private final Logger logger = LoggerUtils.logger(getClass());

    private RabbitMq rabbitMq;

    /**
     * 初始化MQ监听器
     */
    public void init() {
        //根据RabbitMq注解找出使用这个注解的类并初始化消费队列
        this.init(new ArrayList<>(SpringUtil.getBeansWithAnnotation(RabbitMq.class).values()),SpringUtil.getBean(RabbitAdmin.class),SpringUtil.getBean(CachingConnectionFactory.class));
    }

    /**
     * 初始化MQ
     *
     * @param clazzList 消费者集合数组
     */
    private void init(List<Object> clazzList,RabbitAdmin admin, CachingConnectionFactory rabbitConnectionFactory) {
        // 判断消费者队列是否存在
        if (ValidateUtils.isEmpty(clazzList)) {
            logger.info("MessageListenerConfig:没有rabbitMq消费者");
            return;
        }
        BaseMQProfile rabbitProfile = SpringUtil.getBean(BaseMQProfile.class);
        List<String> notEnableQueueList = ValidateUtils.isEmpty(rabbitProfile) || ValidateUtils.isEmpty(rabbitProfile.getNotEnabledQueue()) ?
                                          null : Arrays.asList(rabbitProfile.getNotEnabledQueue().split(","));

        Map<String, AbstractMQService> abstractMQMap = SpringUtil.getBeansOfType(AbstractMQService.class);
        // 遍历消费者队列进行初始化绑定以及监听
        initMq : for (Object abstractConsumer : clazzList) {
            // 根据反射获取rabbitMQ注解信息
            rabbitMq = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), RabbitMq.class);
            String[] queues = rabbitMq.queues();
            for (String queue : queues) {
                /**
                 * 判断配置文件中是否存在去除启动的rabbitmq队列
                 */
                if(ValidateUtils.isNotEmpty(notEnableQueueList) && notEnableQueueList.contains(queue)){
                    logger.info("MessageListenerConfig:不启用队列为:{} 不启用的队列名包含 {} 队列",notEnableQueueList, rabbitMq.queues());
                    continue initMq;
                }
            }

            for (String queueName : queues) {
                // 初始化队列绑定
                Queue queue = initBinding(abstractMQMap,queueName,true, false,admin);
                // 启动监听器并保存已启动的MQ
                RabbitMQInitConfig.allQueueContainerMap.put(queue.getName(), this.startContainer((AbstractConsumer) abstractConsumer, queue,admin,rabbitConnectionFactory));
                // 初始化死信队列
                this.initDlx(queue,admin,rabbitConnectionFactory,abstractMQMap);
            }

        }
    }

    /**
     * 初始化死信队列MQ
     */
    private void initDlx(Queue queue,RabbitAdmin admin, CachingConnectionFactory rabbitConnectionFactory,Map<String, AbstractMQService> abstractMQMap) {
        // 判断消费队列是否需要死信队列 只要死信队列或者延时队列为true即可判断为开启死信队列
        Class<?> clazz = rabbitMq.dlxClazz();

        String queueName = queue.getName();
        if (void.class.equals(clazz)) {
            logger.info("MessageListenerConfig:initDlx 队列:{}不需要死信队列", queueName);
            return;
        }

        // 判断设置死信队列的类必须是为AbstractDlxConsumer下的子类
        if (!AbstractConsumer.class.isAssignableFrom(clazz) || AbstractConsumer.class.equals(clazz)) {
            logger.info("MessageListenerConfig:队列:{}死信队列设置错误,死信队列类名为:{}", queueName, clazz.getName());
            return;
        }
        // 获取死信队列类
        AbstractConsumer abstractConsumer = (AbstractConsumer) SpringUtil.getBean(clazz);
        // 初始化队列绑定
        Queue queues = initBinding(abstractMQMap,queueName,false, true,admin);
        // 启动监听器
        this.startContainer(abstractConsumer, queues,admin,rabbitConnectionFactory);
        logger.info("MessageListenerConfig队列:{}绑定{}死信队列",queue.getName(),queues.getName());
    }

    /**
     * 启动监听器
     *
     * @param abstractConsumer 抽象的消费者
     */
    private DirectMessageListenerContainer startContainer(AbstractConsumer abstractConsumer, Queue queue,RabbitAdmin admin, CachingConnectionFactory rabbitConnectionFactory) {
        // 新建监听器
        DirectMessageListenerContainer container = new DirectMessageListenerContainer(rabbitConnectionFactory);
        // 新建消息侦听器适配器
        MessageListenerAdapter adapter = new MessageListenerAdapter(abstractConsumer);
        // 设置编码格式
        adapter.setEncoding(BaseCode.UTF8);
        // 监听器配置队列
        container.setQueues(queue);
        // 消费者的监听
        container.setMessageListener(adapter);
        // 是否自动声明 队列、交换机、绑定
        container.setAutoDeclare(true);
        // 设置消费者提交方式
        container.setAcknowledgeMode(rabbitMq.mode());
        //为每个队列添加多个消费者 增加并行度
        container.setConsumersPerQueue(rabbitMq.consumersPerQueue());
        container.setPrefetchCount(rabbitMq.prefetchCount());
        container.setAmqpAdmin(admin);
        RabbitMqRetry rabbitMqRetry = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), RabbitMqRetry.class);
        if (ValidateUtils.isNotEmpty(rabbitMqRetry) && AbstractConsumer.class.isAssignableFrom(abstractConsumer.getClass())) {
            //设置重试机制
            container.setAdviceChain(setRabbitRetry(rabbitMqRetry,admin));
        }
        // 启动对应的适配器
        container.start();
        return container;
    }

    public RetryOperationsInterceptor setRabbitRetry(RabbitMqRetry rabbitMqRetry,RabbitAdmin admin) {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicyByProperties(rabbitMqRetry));
        retryTemplate.setRetryPolicy(retryPolicyByProperties(rabbitMqRetry));
        return RetryInterceptorBuilder
                .stateless()
                .retryOperations(retryTemplate)
                .recoverer(new RepublishMessageRecoverer(admin.getRabbitTemplate()))
                .build();
    }

    public ExponentialBackOffPolicy backOffPolicyByProperties(RabbitMqRetry rabbitMqRetry) {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        // 重试间隔
        backOffPolicy.setInitialInterval(rabbitMqRetry.initialInterval());
        // 重试最大间隔
        backOffPolicy.setMaxInterval(rabbitMqRetry.maxInterval());
        // 重试间隔乘法策略
        backOffPolicy.setMultiplier(rabbitMqRetry.multiplier());
        return backOffPolicy;
    }

    public SimpleRetryPolicy retryPolicyByProperties(RabbitMqRetry rabbitMqRetry) {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(rabbitMqRetry.retryNumber());
        return retryPolicy;
    }

    private Queue initBinding(Map<String, AbstractMQService> abstractMQMap,String queue,boolean isInitDlxMap, boolean isAddDlxPrefix,RabbitAdmin admin) {
        AbstractMQService abstractMQService = null;
        if(ValidateUtils.isNotEmpty(rabbitMq) && rabbitMq.isDelayExchange()){
            abstractMQService = abstractMQMap.get("delayedMQService");
        } else {
            abstractMQService = abstractMQMap.get(rabbitMq.exchangeTypes() + BaseMQService.SERVICE_NAME);
        }
        return abstractMQService.initBinding(rabbitMq,queue, admin, isInitDlxMap, isAddDlxPrefix);
    }

}
