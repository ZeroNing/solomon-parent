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
import com.steven.solomon.mqtt.CommonMqttMessageListener;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;

/**
 * Redis消息消费器抽象基类。
 *
 * <p>提供Redis消息队列的消费框架，子类只需实现 {@link #handleMessage(Object)} 方法即可。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>自动反序列化消息体</li>
 *   <li>防重复消费检查</li>
 *   <li>自动设置租户上下文</li>
 *   <li>消费成功/失败日志记录</li>
 * </ul>
 *
 * @param <T> 消息体类型
 * @param <R> 处理结果类型
 */
public abstract class AbstractConsumer<T,R> extends MessageListenerAdapter implements CommonMqttMessageListener<T,R,RedisQueueModel<T>> {

    /** 日志记录器 */
    protected final Logger logger = LoggerUtils.logger(getClass());

    /** 当前消费的主题名 */
    private String topic;

    /** 当前消息的租户编码 */
    protected String tenantCode;

    /**
     * Redis消息接收回调。
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>反序列化消息体</li>
     *   <li>检查是否重复消费</li>
     *   <li>设置租户上下文</li>
     *   <li>调用子类处理逻辑</li>
     *   <li>清理防重复标记并保存消费日志</li>
     * </ol>
     *
     * @param message Redis消息对象
     * @param pattern 订阅模式（可为null）
     */
    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        // 解析消息体和主题名
        String body = new String(message.getBody());
        topic = new String(message.getChannel());

        Throwable throwable = null;
        RedisQueueModel<T> model = null;
        R result = null;
        try {
          // 反序列化消息
          model = conversion(body);
          tenantCode = model.getTenantCode();
          // 判断是否重复消费
          if (checkMessageKey(model)) {
              throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
          }
          // 设置租户上下文，用于后续数据源切换
          if (ValidateUtils.isNotEmpty(tenantCode)) {
              RequestHeaderHolder.setTenantCode(tenantCode);
         }
          logger.info("线程名:{},AbstractConsumer:主题:{},消费者消息: {}", Thread.currentThread().getName(),topic, body);
          // 调用子类的消息处理逻辑
          result = this.handleMessage(model.getBody());
        } catch (Throwable e) {
          // 消费失败记录异常信息
          logger.error("AbstractConsumer:消费报错 异常为:", e);
          throwable = e;
        } finally {
          // 清理防重复标记并保存消费日志
          deleteCheckMessageKey(model);
          saveLog(result,throwable,model);
        }
    }
}
