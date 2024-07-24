package com.steven.solomon.consumer;


import com.steven.solomon.entiy.RedisQueueModel;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.lang.Nullable;

/**
 * Redis消费器
 */
public abstract class AbstractConsumer<T,R> extends MessageListenerAdapter {

    protected final Logger logger = LoggerUtils.logger(getClass());

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        String body = new String(message.getBody());
        String channel = new String(message.getChannel());
        try {
          logger.debug("线程名:{},AbstractConsumer:主题:{},消费者消息: {}", Thread.currentThread().getName(),channel, body);
          RedisQueueModel model = JackJsonUtils.conversionClass(body, RedisQueueModel.class);
          RequestHeaderHolder.setTenantCode(model.getTenantCode());
          R result = this.handleMessage((T) model.getBody());
          // 保存消费成功消息
          saveLog(result, message, model);
        } catch (Throwable e){
          // 消费失败次数不等于空并且失败次数大于某个次数,不处理直接return,并记录到数据库
          logger.error("AbstractConsumer:消费报错 异常为:", e);
          // 将消费失败的记录保存到数据库或者不处理也可以
          this.saveFailMessage(message, e);
        }
    }

    /**
     * 消费方法
     * @param body 请求数据
     */
    public abstract R handleMessage(T body) throws Exception;

    /**
     * 保存消费失败的消息
     *
     * @param message mq所包含的信息
     * @param e       异常
     */
    public void saveFailMessage(Message message, Throwable e) {

    }

    /**
     * 保存消费成功消息
     */
    public abstract void saveLog(R result, Message message, RedisQueueModel model);
}
