package com.steven.solomon.consumer;


import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.entiy.RedisQueueModel;
import com.steven.solomon.holder.RequestHeaderHolder;
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
        String topic = new String(message.getChannel());
        Throwable throwable = null;
        RedisQueueModel<T> model = null;
        R result = null;
        try {
          logger.info("线程名:{},AbstractConsumer:主题:{},消费者消息: {}", Thread.currentThread().getName(),topic, body);
          model = JSONUtil.toBean(body, new TypeReference<RedisQueueModel<T>>(){},true);
          RequestHeaderHolder.setTenantCode(model.getTenantCode());
          result = this.handleMessage(model.getBody(),topic);
        } catch (Throwable e){
          // 消费失败次数不等于空并且失败次数大于某个次数,不处理直接return,并记录到数据库
          logger.error("AbstractConsumer:消费报错 异常为:", e);
          throwable = e;
        } finally {
          // 保存消费成功/失败的消息
          saveLog(result, topic, model,throwable);
        }
    }

    /**
     * 消费方法
     */
    public abstract R handleMessage(T body,String topic) throws Exception;

    /**
     * 保存消费成功消息
     */
    public abstract void saveLog(R result, String topic, RedisQueueModel<T> model, Throwable e);
}
