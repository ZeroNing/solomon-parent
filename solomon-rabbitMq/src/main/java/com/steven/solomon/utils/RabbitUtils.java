package com.steven.solomon.utils;

import cn.hutool.core.annotation.AnnotationUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.code.RabbitMqErrorCode;
import com.steven.solomon.consumer.AbstractConsumer;
import com.steven.solomon.entity.MessageQueueDetail;
import com.steven.solomon.entity.RabbitMqModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.init.RabbitMQInitConfig;
import com.steven.solomon.properties.RabbitMqProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.pojo.entity.BaseMq;
import com.steven.solomon.service.SendService;
import com.steven.solomon.verification.ValidateUtils;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
@EnableConfigurationProperties(value = {RabbitProperties.class,RabbitMqProperties.class})
public class RabbitUtils implements SendService<RabbitMqModel<?>> {

    private final Logger logger = LoggerUtils.logger(getClass());

    private final RabbitTemplate rabbitTemplate;

    private final boolean enabled;

    public RabbitUtils(RabbitMqProperties rabbitMqProperties, ApplicationContext context) {
        SpringUtil.setContext(context);
        this.enabled = rabbitMqProperties.getEnabled();
        this.rabbitTemplate = !enabled ? null : SpringUtil.getBean(RabbitTemplate.class);
    }

    /**
     * 发送消息
     */
    @Override
    public void send(RabbitMqModel<?> mq) throws Exception {
        if(!enabled){
            logger.error("Rabbitmq不开启,禁止使用该功能");
            return;
        }
        if (!convertAndSend(mq, 0, false)) {
            throw new BaseException(BaseExceptionCode.BASE_EXCEPTION_CODE);
        }
    }

    /**
     * 发送延缓信息
     */
    @Override
    public void sendDelay(RabbitMqModel<?> mq, long delay) throws Exception {
        if(!enabled){
            logger.error("Rabbitmq不开启,禁止使用该功能");
            return;
        }
        if (!convertAndSend(mq, delay, true)) {
            throw new BaseException(BaseExceptionCode.BASE_EXCEPTION_CODE);
        }
    }

    /**
     * 发送消息,并设置消息过期时间
     */
    @Override
    public void sendExpiration(RabbitMqModel<?> mq, long expiration) throws Exception {
        if(!enabled){
            logger.error("Rabbitmq不开启,禁止使用该功能");
            return;
        }
        if (!convertAndSend(mq, expiration, false)) {
            throw new BaseException(BaseExceptionCode.BASE_EXCEPTION_CODE);
        }
    }

    /**
     * 重置队列并发使用者
     */
    public boolean resetQueueConcurrentConsumers(String queueName, int concurrentConsumers) throws BaseException {
        if(!enabled){
            logger.error("Rabbitmq不开启,禁止使用该功能");
            return false;
        }
        Assert.state(concurrentConsumers > 0, "参数 'concurrentConsumers' 必须大于0.");
        DirectMessageListenerContainer container = (DirectMessageListenerContainer) findContainerByQueueName(queueName);
        if (ValidateUtils.isNotEmpty(container) && container.isActive() && container.isRunning()) {
            container.setConsumersPerQueue(concurrentConsumers);
            return true;
        }
        return false;
    }

    /**
     * 重启消息监听者
     */
    public boolean restartMessageListener(String queueName) throws BaseException {
        if(!enabled){
            logger.error("Rabbitmq不开启,禁止使用该功能");
            return false;
        }
        if (ValidateUtils.isEmpty(queueName)) {
            logger.error("restartMessageListener 重启队列失败,传入队列名为空!");
            return false;
        }
        DirectMessageListenerContainer container = (DirectMessageListenerContainer) findContainerByQueueName(queueName);
        if (ValidateUtils.isEmpty(container)) {
            logger.error("restartMessageListener 停止队列失败,没有这个监听器");
            return false;
        }
        Assert.state(!container.isRunning(), String.format("消息队列%s对应的监听容器正在运行！", queueName));
        container.start();
        return true;
    }

    /**
     * 停止消息监听者
     */
    public boolean stopMessageListener(String queueName) throws BaseException {
        if(!enabled){
            logger.error("Rabbitmq不开启,禁止使用该功能");
            return false;
        }
        if (ValidateUtils.isEmpty(queueName)) {
            logger.error("stopMessageListener 停止队列失败,传入队列名为空!");
            return false;
        }
        DirectMessageListenerContainer container = (DirectMessageListenerContainer) findContainerByQueueName(queueName);
        if (ValidateUtils.isEmpty(container)) {
            logger.error("stopMessageListener 停止队列失败,没有这个监听器");
            return false;
        }
        Assert.state(container.isRunning(), String.format("消息队列%s对应的监听容器未运行！", queueName));
        container.stop();
        return true;
    }

    //  private Map<String, AbstractMessageListenerContainer> getQueue2ContainerAllMap() {
//    if (!hasInit) {
//      synchronized (allQueueContainerMap) {
//        if (!hasInit) {
//          Collection<MessageListenerContainer> listenerContainers = SpringUtil.getBean(RabbitListenerEndpointRegistry.class).getListenerContainers();
//          listenerContainers.forEach(container -> {
//                    	DirectMessageListenerContainer simpleContainer = (DirectMessageListenerContainer) container;
//                        Arrays.stream(simpleContainer.getQueueNames()).forEach(queueName ->
//                        allQueueContainerMap.putIfAbsent(StringUtils.trim(queueName), simpleContainer));
//                    });
//          hasInit = true;
//        }
//      }
//    }
//    return allQueueContainerMap;
//  }

    private boolean convertAndSend(BaseMq<?> baseMq, long expiration, boolean isDelayed) throws BaseException {
        if(!enabled){
            logger.error("rabbitmq没开启,不发送消息");
            return false;
        }
        RabbitMqModel<?> rabbitMQModel = (RabbitMqModel<?>) baseMq;
        if (ValidateUtils.isEmpty(rabbitMQModel) || ValidateUtils.isEmpty(rabbitMQModel.getExchange())) {
            return false;
        }
        Map<String, Object> headers = rabbitMQModel.getHeaders();
        rabbitTemplate.convertAndSend(rabbitMQModel.getExchange(), rabbitMQModel.getRoutingKey(), rabbitMQModel, msg -> {
            //设置消息持久化
            msg.getMessageProperties().setDeliveryMode(rabbitMQModel.getMessagePersistent() ? MessageDeliveryMode.PERSISTENT : MessageDeliveryMode.NON_PERSISTENT);
            //设置消息优先级
            msg.getMessageProperties().setPriority(rabbitMQModel.getPriority());
            if (ValidateUtils.equals(0L, expiration)) {
                return msg;
            }
            if (ValidateUtils.isNotEmpty(headers)) {
                for (Entry<String, Object> entry : headers.entrySet()) {
                    msg.getMessageProperties().setHeader(entry.getKey(), entry.getValue());
                }
            }
            if (isDelayed) {
                msg.getMessageProperties().setHeader("x-delay", expiration);
            } else {
                msg.getMessageProperties().setExpiration(String.valueOf(expiration));
            }
            return msg;
        }, new CorrelationData());
        return true;
    }

    public Collection<AbstractMessageListenerContainer> getAllQueueContainerList() {
        return RabbitMQInitConfig.allQueueContainerMap.values();
    }

    public List<MessageQueueDetail> statAllMessageQueueDetail() {
        List<MessageQueueDetail> queueDetailList = new ArrayList<>();
        RabbitMQInitConfig.allQueueContainerMap.entrySet().forEach(entry -> {
            String queueName = entry.getKey();
            AbstractMessageListenerContainer container = entry.getValue();
            MessageQueueDetail deatil = new MessageQueueDetail(queueName, container);
            queueDetailList.add(deatil);
        });

        return queueDetailList;
    }

    public AbstractMessageListenerContainer findContainerByQueueName(String queueName) {
        String key = StringUtils.trim(queueName);
        return RabbitMQInitConfig.allQueueContainerMap.get(key);
    }

    /**
     * 手动拉去消息消费,根据队列名找到对应消费器消费
     *
     * @param transactional 是否开启事务
     * @param queueName     队列名
     */
    public void handleQueueMessageManually(boolean transactional, String queueName) throws Exception {
        if(!enabled){
            logger.error("rabbitmq没开启,不发送消息");
            return;
        }
        Channel channel = rabbitTemplate.getConnectionFactory().createConnection().createChannel(transactional);
        GetResponse response = channel.basicGet(queueName, false);
        if (ValidateUtils.isEmpty(response)) {
            logger.error("没有从{}队列中获取到消息", queueName);
            return;
        }
        Map<String, Object> annotationMap = SpringUtil.getBeansWithAnnotation(MessageListener.class);
        for (Object obj : annotationMap.values()) {
            MessageListener messageListener = AnnotationUtil.getAnnotation(obj.getClass(), MessageListener.class);
            if(ValidateUtils.isEmpty(messageListener)){
                continue;
            }
            List<String> queues = Arrays.asList(messageListener.queues());
            if (!queues.contains(queueName)) {
                continue;
            }
            logger.info("从{}队列找到消费者类:{}", queueName, obj.getClass().getSimpleName());
            AbstractConsumer<?,?> abstractConsumer = (AbstractConsumer<?,?>) obj;
            abstractConsumer.onMessage(new Message(response.getBody(), new MessageProperties()), channel);
        }
    }

    /**
     * 请求回复消息发送
     */
    public Object convertSendAndReceive(RabbitMqModel<?> model) throws BaseException {
        if(!enabled){
            logger.error("rabbitmq没开启,不发送消息");
            return null;
        }
        if(ValidateUtils.isEmpty(model.getReplyTo())){
            throw new BaseException(RabbitMqErrorCode.REPLY_TO_IS_NULL);
        }
        return rabbitTemplate.convertSendAndReceive(model.getRoutingKey(), model, message -> {
            message.getMessageProperties().setReplyTo(model.getReplyTo());
            message.getMessageProperties().setCorrelationId(UUID.randomUUID().toString());
            return message;
        });
    }

    /**
     * 回复消息发送
     */
    public void sendReplyTo(String routingKey, final Message object) throws AmqpException {
        if(!enabled){
            logger.error("rabbitmq没开启,不发送消息");
            return;
        }
        rabbitTemplate.send(routingKey, object);
    }
}