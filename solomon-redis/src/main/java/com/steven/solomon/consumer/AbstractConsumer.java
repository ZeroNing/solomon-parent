package com.steven.solomon.consumer;


import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.entiy.RedisQueueModel;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.mq.CommonMqttMessageListener;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;

/**
 * Redis消费器
 */
public abstract class AbstractConsumer<T,R> extends MessageListenerAdapter implements CommonMqttMessageListener<T,R,RedisQueueModel<T>> {

    protected final Logger logger = LoggerUtils.logger(getClass());

    private String topic;

    protected String tenantCode;

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        String body = new String(message.getBody());
        topic = new String(message.getChannel());

        Throwable throwable = null;
        RedisQueueModel<T> model = null;
        R result = null;
        try {
          model = conversion(body);
          tenantCode = model.getTenantCode();
          // 判断是否重复消费
          if(checkMessageKey(model)){
              throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
          }
          if(ValidateUtils.isNotEmpty(tenantCode)){
              RequestHeaderHolder.setTenantCode(tenantCode);
         }
          logger.info("线程名:{},AbstractConsumer:主题:{},消费者消息: {}", Thread.currentThread().getName(),topic, body);
          result = this.handleMessage(model.getBody());
        } catch (Throwable e){
          // 消费失败次数不等于空并且失败次数大于某个次数,不处理直接return,并记录到数据库
          logger.error("AbstractConsumer:消费报错 异常为:", e);
          throwable = e;
        } finally {
          // 保存消费成功/失败的消息
          deleteCheckMessageKey(model);
          saveLog(result,throwable,model);
        }
    }
}
