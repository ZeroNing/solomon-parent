package com.steven.solomon.mqtt;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.code.MqErrorCode;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.context.TenantRequestBinder;
import com.steven.solomon.context.TenantResourceScope;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.pojo.entity.BaseMq;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;

/**
 * MQTT 消费者公共模板。
 *
 * <p>统一处理消息反序列化、重复消费校验、租户上下文设置和消费日志回调。</p>
 *
 * @param <T> 业务消息体类型
 * @param <R> 消费结果类型
 * @param <M> MQTT 消息模型类型
 */
public abstract class AbstractMqttConsumerSupport<T, R, M extends BaseMq<T>>
    implements CommonMqttMessageListener<T, R, M> {

  protected final Logger logger = LoggerUtils.logger(getClass());

  protected String topic;

  protected String tenantCode;

  /**
   * 返回当前客户端实现使用的消息模型类型。
   */
  protected abstract Class<M> messageModelType();

  @Override
  public M conversion(String json) {
    M model = JSONUtil.toBean(json, messageModelType(), true);
    T body = resolveBody(json, model.getBody());
    model.setBody(body);
    return model;
  }

  /**
   * 消费原始 MQTT 字节消息。
   *
   * @param topic 订阅主题
   * @param payload MQTT 消息体
   */
  protected void consumeMessage(String topic, byte[] payload) throws Exception {
    this.topic = topic;

    String json = new String(payload, StandardCharsets.UTF_8);
    Throwable throwable = null;
    R result = null;
    M model = null;
    TenantResourceScope tenantResourceScope = null;
    try {
      model = conversion(json);
      tenantCode = SpringUtil.getBean(TenantModeResolver.class).resolve(model.getTenantCode());
      logger.info(
          "线程名:{}, 租户编码:{}, 消息ID:{}, topic主题:{}, MQTT消费者消息:{}",
          Thread.currentThread().getName(),
          tenantCode,
          model.getMsgId(),
          topic,
          json);
      if (checkMessageKey(model)) {
        throw new BaseException(MqErrorCode.MESSAGE_REPEAT_CONSUMPTION);
      }
      if (ObjectUtil.isNotEmpty(tenantCode)) {
        RequestHeaderHolder.setTenantCode(tenantCode);
        tenantResourceScope = TenantResourceScope.open(tenantCode,
            SpringUtil.getBeansOfType(TenantRequestBinder.class).values());
      }
      result = handleMessage(model.getBody());
    } catch (Throwable e) {
      logger.error("MQTT消费者消费失败, 消息:{}, 异常:", json, e);
      throwable = e;
    } finally {
      try {
        deleteCheckMessageKey(model);
        saveLog(result, throwable, model);
      } finally {
        try {
          if (tenantResourceScope != null) {
            tenantResourceScope.close();
          }
        } finally {
          RequestHeaderHolder.remove();
        }
      }
    }
  }
}
