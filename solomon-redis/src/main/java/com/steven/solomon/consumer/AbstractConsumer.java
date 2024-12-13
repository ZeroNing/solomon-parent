package com.steven.solomon.consumer;


import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.entiy.RedisQueueModel;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.mq.CommonMqttMessageListener;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;

/**
 * Redis消费器
 */
public abstract class AbstractConsumer<T,R> extends MessageListenerAdapter implements CommonMqttMessageListener<T,R> {

    protected final Logger logger = LoggerUtils.logger(getClass());

    private String topic;

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        String body = new String(message.getBody());
        topic = new String(message.getChannel());

        Throwable throwable = null;
        RedisQueueModel<T> model = null;
        R result = null;
        try {
          model = conversion(body);
          RequestHeaderHolder.setTenantCode(model.getTenantCode());
          logger.info("线程名:{},AbstractConsumer:主题:{},消费者消息: {}", Thread.currentThread().getName(),topic, body);
          result = this.handleMessage(model.getBody());
        } catch (Throwable e){
          // 消费失败次数不等于空并且失败次数大于某个次数,不处理直接return,并记录到数据库
          logger.error("AbstractConsumer:消费报错 异常为:", e);
          throwable = e;
        } finally {
          // 保存消费成功/失败的消息
          saveLog(result, model,throwable);
        }
    }

    /**
     * 保存消费成功消息
     */
    public abstract void saveLog(R result, RedisQueueModel<T> model, Throwable e);

    private RedisQueueModel<T> conversion(String json) {
        RedisQueueModel<T> model = JSONUtil.toBean(json, new TypeReference<RedisQueueModel<T>>() {},true);
        T body = model.getBody();
        boolean isJsonObject = body instanceof JSONObject;
        boolean isJsonArray = body instanceof JSONArray;
        if(!isJsonObject && !isJsonArray){
            return model;
        }
        Type typeArgument = TypeUtil.getTypeArgument(getClass(),0);
        body = JSONUtil.toBean(JSONUtil.toJsonStr(body),typeArgument,true);
        model.setBody(body);
        return model;
    }
}
